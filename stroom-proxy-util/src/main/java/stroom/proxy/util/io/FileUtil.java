package stroom.proxy.util.io;

import stroom.proxy.util.config.StroomProperties;
import stroom.proxy.util.logging.StroomLogger;
import stroom.proxy.util.thread.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileUtil {
    private final static int MKDIR_RETRY_COUNT = 2;
    private final static int MKDIR_RETRY_SLEEP_MS = 100;
    private static final StroomLogger LOGGER = StroomLogger.getLogger(FileUtil.class);
    /**
     * JVM wide temp dir
     */
    private volatile static Path tempDir = null;

    private FileUtil() {
        // Utility.
    }

    public static Path getInitialTempPath() {
        final String pathString = StroomProperties.getProperty(StroomProperties.STROOM_TEMP);
        if (pathString == null) {
            throw new RuntimeException("No temp path is specified");
        }

        final Path path = Paths.get(pathString);
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                LOGGER.fatal("Unable to create temp directory.", e);
                throw new RuntimeException("Unable to create temp directory", e);
            }
        }

        return path;
    }

    public static Path getTempPath() {
        if (tempDir == null) {
            synchronized (FileUtil.class) {
                if (tempDir == null) {
                    tempDir = getInitialTempPath();
                }
            }
        }

        return tempDir;
    }

    public static File getInitialTempDir() {
        return getInitialTempPath().toFile();
    }

    public static File getTempDir() {
        return getTempPath().toFile();
    }

    public static void forgetTempDir() throws IOException {
        synchronized (FileUtil.class) {
            tempDir = null;
        }
    }

    public static void createNewFile(final File file) throws IOException {
        if (!file.createNewFile()) {
            throw new FileUtilException("Unable to create new file: " + file.getAbsolutePath());
        }
    }

    public static void deleteFile(final File file) {
        if (file.exists()) {
            if (!file.isFile()) {
                throw new FileUtilException("Path is directory not file \"" + file.getAbsolutePath() + "\"");
            }

            if (!file.delete()) {
                throw new FileUtilException("Unable to delete \"" + file.getAbsolutePath() + "\"");
            }
        }
    }

    public static void forceDelete(final File file) {
        if (file != null && file.exists()) {
            if (file.isDirectory()) {
                for (final File f : file.listFiles()) {
                    forceDelete(f);
                }
            }

            try {
                if (!file.delete()) {
                    file.deleteOnExit();
                }
            } catch (final Exception e) {
            }
        }
    }

    public static void mkdirs(final File dir) {
        if (!dir.isDirectory()) {
            if (!doMkdirs(null, dir, MKDIR_RETRY_COUNT)) {
                throw new FileUtilException("Unable to make directory: " + dir.getAbsolutePath());
            }

            if (!dir.isDirectory()) {
                throw new FileUtilException("Directory not found: " + dir.getAbsolutePath());
            }
        }
    }

    public static boolean doMkdirs(final File superDir, final File dir, int retry) {
        // Make sure the parent exists first
        final File parentDir = dir.getParentFile();
        if (parentDir != null && !parentDir.isDirectory()) {
            if (superDir != null && superDir.equals(parentDir)) {
                // Unable to make parent as it is the super dir
                return false;

            }
            if (!doMkdirs(superDir, parentDir, retry)) {
                // Unable to make parent :(
                return false;
            }
        }
        // No Make us
        if (!dir.isDirectory()) {
            // * CONCURRENT PROBLEM AREA *
            if (!dir.mkdir()) {
                // Someone could have made it in the * CONCURRENT PROBLEM AREA *
                if (!dir.isDirectory()) {
                    if (retry > 0) {
                        retry = retry - 1;
                        LOGGER.warn("doMkdirs() - Sleep and Retry due to unable to create " + dir.getAbsolutePath());
                        ThreadUtil.sleep(MKDIR_RETRY_SLEEP_MS);
                        return doMkdirs(superDir, dir, retry);
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;

    }

    public static String getCanonicalPath(final File file) {
        try {
            return file.getCanonicalPath();
        } catch (final IOException e) {
        }

        return file.getAbsolutePath();
    }

}
