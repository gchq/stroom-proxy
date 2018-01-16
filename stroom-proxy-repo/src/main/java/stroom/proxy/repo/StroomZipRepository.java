/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.proxy.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.feed.MetaMap;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

/**
 * Class that represents a repository on the file system. By default files are
 * created in this repo using the stroom id structure where 1000 files are stored
 * per dir and dir's are created by padding the id to multiplier of 3 and using
 * each 3 part as a dir separator.
 */
public class StroomZipRepository {
    final static String LOCK_EXTENSION = ".lock";
    final static String ZIP_EXTENSION = ".zip";
    private final static String ERROR_EXTENSION = ".err";
    final static String BAD_EXTENSION = ".bad";

    private final static Logger LOGGER = LoggerFactory.getLogger(StroomZipRepository.class);

    private static final String DEFAULT_REPOSITORY_FORMAT = "${pathId}/${id}";
    private static final String ID_VAR = "${id}";

    // 1 hour
    private final static int DEFAULT_LOCK_AGE_MS = 1000 * 60 * 60;
    // Ten seconds
    private final static int TEN_SECONDS = 1000 * 10;

    private final AtomicLong fileCount = new AtomicLong(0);
    private final AtomicBoolean finish = new AtomicBoolean(false);
    private final int lockDeleteAgeMs;

    private final String repositoryFormat;

    /**
     * Name of the repository while open
     */
    private Path baseLockDir;

    /**
     * Final name once finished (may be null)
     */
    private Path baseResultantDir;

    public StroomZipRepository(final String dir) {
        this(dir, null, false, DEFAULT_LOCK_AGE_MS);
    }

//    /**
//     * Open a repository (with or without locking) with the default repository format.
//     */
//    public StroomZipRepository(final String dir, final boolean lock, final int lockDeleteAgeMs) {
//        this(dir, null, lock, lockDeleteAgeMs);
//    }

    /**
     * Open a repository (with or without locking).
     */
    StroomZipRepository(final String dir, final String repositoryFormat, final boolean lock, final int lockDeleteAgeMs) {
        if (repositoryFormat == null || repositoryFormat.trim().length() == 0) {
            LOGGER.info("Using default repository format: " + DEFAULT_REPOSITORY_FORMAT);
            this.repositoryFormat = DEFAULT_REPOSITORY_FORMAT;

        } else {
            LOGGER.info("Using repository format: " + repositoryFormat);

            // Validate the proxy repository format.
            final int index = repositoryFormat.indexOf(ID_VAR);
            if (index == -1) {
                throw new RuntimeException("The proxy repository format must contain " + ID_VAR);
            }
            if (index > 0) {
                final char c = repositoryFormat.charAt(index - 1);
                if (c != '/' && c != '_') {
                    throw new RuntimeException("The " + ID_VAR + " replacement variable in the proxy repository format must be proceeded by '/' or '_'");
                }
            }
            if (index + ID_VAR.length() < repositoryFormat.length()) {
                final char c = repositoryFormat.charAt(index + ID_VAR.length());
                if (c != '_') {
                    throw new RuntimeException("The ${id} replacement variable in the proxy repository format must be followed by '_'");
                }
            }

            this.repositoryFormat = repositoryFormat;
        }

        this.lockDeleteAgeMs = lockDeleteAgeMs;
        if (lock) {
            baseLockDir = Paths.get(dir + LOCK_EXTENSION);
            baseResultantDir = Paths.get(dir);
            if (Files.isDirectory(baseResultantDir)) {
                throw new RuntimeException("Rolled directory already exists " + baseResultantDir);
            }
        } else {
            baseLockDir = Paths.get(dir);
        }

        // Create the root directory
        if (!Files.isDirectory(baseLockDir)) {
            try {
                Files.createDirectories(baseLockDir);
            } catch (final IOException e) {
                throw new RuntimeException("Unable to create directory " + baseLockDir);
            }
        }

        // We may be an existing repository so check for the last ID.
        scanRepository((min, max) -> {
            LOGGER.info("First repository id = " + min);
            LOGGER.info("Last repository id = " + max);
            fileCount.set(max);
        });

        LOGGER.debug("() - Opened REPO {} lastId = {}", baseLockDir, fileCount.get());
    }

    /**
     * Gets the minimum and maximum repository ids
     *
     * @param consumer The consumer for the min and max values.
     */
    void scanRepository(final BiConsumer<Long, Long> consumer) {
        long firstFileId = 0;
        long lastFileId = 0;

        // We may be an existing repository so check for the last ID.
        LOGGER.info("Scanning repository to find existing files");
        final AtomicLong minId = new AtomicLong(Long.MAX_VALUE);
        final AtomicLong maxId = new AtomicLong(Long.MIN_VALUE);

        final Path path = getRootDir();
        if (path != null && Files.isDirectory(path)) {
            try {
                Files.walkFileTree(path, new ZipFileVisitor() {
                    @Override
                    FileVisitResult matchingFile(final Path file, final BasicFileAttributes attrs) {
                        LOGGER.debug("Examining " + file.toString());

                        final String idString = getIdPart(file);
                        if (idString.length() == 0) {
                            LOGGER.warn("File is not a valid repository file " + file.toString());
                        } else {
                            final long id = Long.valueOf(idString);

                            boolean success = false;
                            while (!success) {
                                final long min = minId.get();
                                success = id >= min || minId.compareAndSet(min, id);
                            }

                            success = false;
                            while (!success) {
                                final long max = maxId.get();
                                success = id <= max || maxId.compareAndSet(max, id);
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (final IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }

        if (minId.get() < Long.MAX_VALUE) {
            firstFileId = minId.get();
        }
        if (maxId.get() > Long.MIN_VALUE) {
            lastFileId = maxId.get();
        }

        consumer.accept(firstFileId, lastFileId);
    }

    /**
     * @return last sequence or count in this repository.
     */
    long getFileCount() {
        return fileCount.get();
    }

    /**
     * @param newCount new higher sequencer (used during testing)
     */
    synchronized void setCount(final long newCount) {
        if (fileCount.get() > newCount) {
            throw new IllegalArgumentException("Can't reduce the size of count");
        }
        fileCount.set(newCount);
    }

    private String getIdPart(final Path path) {
        final String fileName = path.getFileName().toString();

        // Turn the file name into a char array.
        final char[] chars = fileName.toCharArray();

        // Find the index of the first non digit character.
        int index = -1;
        for (int i = 0; i < chars.length && index == -1; i++) {
            if (!Character.isDigit(chars[i])) {
                index = i;
            }
        }

        // If we found a non digit character at a position greater than 0
        // but that is a modulus of 3 (id's are of the form 001 or 001001 etc)
        // then this is a valid repository zip file.
        if (index > 0 && index % 3 == 0) {
            return fileName.substring(0, index);
        }

        return "";
    }

    Path getRootDir() {
        if (baseResultantDir != null) {
            return baseResultantDir;
        }
        return baseLockDir;
    }

    synchronized void finish() {
        if (!finish.get()) {
            finish.set(true);
            removeLock();
        }
    }

    StroomZipOutputStream getStroomZipOutputStream() throws IOException {
        return getStroomZipOutputStream(null);
    }

    public StroomZipOutputStream getStroomZipOutputStream(final MetaMap metaMap)
            throws IOException {
        if (finish.get()) {
            throw new RuntimeException("No longer allowed to write new streams to a finished repository");
        }
        final String filename = StroomFileNameUtil.constructFilename(fileCount.incrementAndGet(), repositoryFormat,
                metaMap, ZIP_EXTENSION);
        final Path file = baseLockDir.resolve(filename);


        StroomZipOutputStreamImpl outputStream;

        // Create directories and files in a synchronized way so that the clean() method will not remove empty
        // directories that we are just about to write to.
        synchronized (StroomZipRepository.this) {
            final Path dir = file.getParent();
            // Ensure parent dir's exist
            Files.createDirectories(dir);

            outputStream = new StroomZipOutputStreamImpl(file);
        }

        return outputStream;
    }

    private Path getErrorFile(final StroomZipFile zipFile) {
        final Path file = zipFile.getFile();
        final String fileName = file.getFileName().toString();
        if (fileName.endsWith(BAD_EXTENSION)) {
            return file.getParent().resolve(fileName.substring(0, fileName.length() - ZIP_EXTENSION.length() - BAD_EXTENSION.length())
                    + ERROR_EXTENSION + BAD_EXTENSION);
        } else {
            return file.getParent().resolve(fileName.substring(0, fileName.length() - ZIP_EXTENSION.length()) + ERROR_EXTENSION);
        }
    }

    @SuppressWarnings(value = "DM_DEFAULT_ENCODING")
    public void addErrorMessage(final StroomZipFile zipFile, final String msg, final boolean bad) {
        final Path file = zipFile.getFile();

        try {
            Path errorFile = getErrorFile(zipFile);
            if (!Files.isRegularFile(file)) {
                return;
            }

            if (bad) {
                final Path renamedFile = file.getParent().resolve(file.getFileName().toString() + BAD_EXTENSION);
                try {
                    zipFile.renameTo(renamedFile);
                } catch (final Exception e) {
                    LOGGER.warn("Failed to rename zip file to " + renamedFile);
                }
                if (Files.isRegularFile(errorFile)) {
                    final Path renamedErrorFile = errorFile.getParent().resolve(errorFile.getFileName().toString() + BAD_EXTENSION);
                    Files.move(errorFile, renamedErrorFile);
                    errorFile = renamedErrorFile;
                }
            }

            try (final OutputStream os = Files.newOutputStream(errorFile)) {
                os.write(msg.getBytes(CharsetConstants.DEFAULT_CHARSET));
            }

        } catch (final IOException ex) {
            LOGGER.warn("Failed to write to file " + zipFile + " message " + msg);
        }
    }

    void clean() {
        LOGGER.debug("clean() " + baseLockDir);
        clean(baseLockDir);
    }

    private void clean(final Path path) {
        if (Files.isDirectory(path)) {
            final long tenSecondsAgoMs = System.currentTimeMillis() - TEN_SECONDS;
            final long oldestLockFileMs = System.currentTimeMillis() - lockDeleteAgeMs;

            try {
                Files.walkFileTree(path, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                        try {
                            if (file != null && attrs != null && file.toString().endsWith(".zip.lock")) {
                                final FileTime lastModified = attrs.lastModifiedTime();
                                if (lastModified != null && lastModified.toMillis() < oldestLockFileMs) {
                                    try {
                                        Files.delete(file);
                                        LOGGER.info("Removed old lock file due to age " + file.toString() + " " + DateUtil.createNormalDateTimeString());
                                    } catch (final IOException e) {
                                        LOGGER.error("Unable to remove old lock file due to age " + file.toString());
                                    }
                                }
                            }
                        } catch (final Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(final Path file, final IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
                        try {
                            // Only try and delete directories that are at least 10 seconds old.
                            final FileTime lastModified = getLastModifiedTime(dir);
                            if (lastModified != null && lastModified.toMillis() < tenSecondsAgoMs) {
                                // Synchronize deletion of directories so that the getStroomOutputStream() method has a
                                // chance to create dirs and place files inside them before this method cleans them up.
                                synchronized (StroomZipRepository.this) {
                                    // Have a go at deleting this directory if it is empty and not just about to be written to.
                                    delete(dir);
                                }
                            }
                        } catch (final Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException("Unable to clear directory " + path, e);
            }
        }
    }

    private FileTime getLastModifiedTime(final Path path) {
        try {
            return Files.getLastModifiedTime(path);
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }

        return null;
    }

    private void removeLock() {
        if (baseResultantDir != null) {
            try {
                Files.move(baseLockDir, baseResultantDir);
            } catch (final IOException e) {
                throw new RuntimeException("Unable to rename directory " + baseLockDir + " to " + baseResultantDir);
            }
            baseResultantDir = null;
            // No-longer locked
            baseLockDir = null;
        }
    }

    boolean deleteIfEmpty() {
        if (deleteEmptyDir(baseLockDir)) {
            LOGGER.debug("deleteIfEmpty() - Removed " + baseLockDir);
            return true;
        }
        return false;
    }

    private boolean deleteEmptyDir(final Path path) {
        boolean success = true;

        if (Files.isDirectory(path)) {
            LOGGER.debug("Attempting to delete dir: " + path.toString());

            // Delete all empty sub directories.
            try {
                Files.walkFileTree(path, new AbstractFileVisitor() {
                    @Override
                    public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
                        try {
                            Files.delete(dir);
                        } catch (final IOException e) {
                            LOGGER.debug(e.getMessage(), e);
                        }

                        return super.postVisitDirectory(dir, exc);
                    }
                });
            } catch (final IOException e) {
                LOGGER.error(e.getMessage(), e);
            }

            // See if we removed the directory.
            success = !Files.isDirectory(path);

            if (success) {
                LOGGER.debug("Deleted dir: " + path.toString());
            } else {
                LOGGER.debug("Failed to delete dir: " + path.toString());
            }
        }

        return success;
    }

    private boolean delete(final Path path) {
        try {
            LOGGER.trace("Attempting to delete: " + path.toString());
            Files.delete(path);
            return true;
        } catch (final DirectoryNotEmptyException e) {
            LOGGER.trace("Unable to delete dir as it was not empty: " + path.toString());
        } catch (final IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return false;
    }

    void delete(final StroomZipFile zipFile) {
        try {
            // Delete the file.
            final Path errorfile = getErrorFile(zipFile);
            zipFile.delete();
            if (Files.isRegularFile(errorfile)) {
                Files.delete(errorfile);
            }
        } catch (final IOException ioEx) {
            LOGGER.error("delete() - Unable to delete zip file " + zipFile.getFile(), ioEx);
        }
    }

    static abstract class ZipFileVisitor extends ExtensionFileVisitor {
        ZipFileVisitor() {
            super(ZIP_EXTENSION);
        }
    }

    static abstract class ExtensionFileVisitor extends AbstractFileVisitor {
        private final String extension;

        public ExtensionFileVisitor(final String extension) {
            this.extension = extension;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
            if (file != null && file.toString().endsWith(extension)) {
                return matchingFile(file, attrs);
            }
            return FileVisitResult.CONTINUE;
        }

        abstract FileVisitResult matchingFile(final Path file, final BasicFileAttributes attrs);
    }

    static abstract class AbstractFileVisitor implements FileVisitor<Path> {
        @Override
        public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(final Path file, final IOException exc) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
            return FileVisitResult.CONTINUE;
        }
    }
}
