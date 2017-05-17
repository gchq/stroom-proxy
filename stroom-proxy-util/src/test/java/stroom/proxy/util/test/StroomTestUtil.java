package stroom.proxy.util.test;

import org.apache.commons.io.FileUtils;
import stroom.proxy.util.io.FileUtil;
import stroom.proxy.util.thread.ThreadUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class StroomTestUtil {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    public static File createRootTestDir(final File tempDir) throws IOException {
        return tempDir;
    }

    public static File createSingleTestDir(final File parentDir) throws IOException {
        if (!parentDir.isDirectory()) {
            throw new IOException("The parent directory '" + FileUtil.getCanonicalPath(parentDir) + "' does not exist");
        }

        if (parentDir.getName().equals("test")) {
            return parentDir;
        }

        final File dir = new File(parentDir, "test");
        dir.mkdir();

        if (!dir.isDirectory()) {
            throw new IOException("The test directory '" + FileUtil.getCanonicalPath(dir) + "' does not exist");
        }

        return dir;
    }

    public static File createPerThreadTestDir(final File parentDir) throws IOException {
        if (!parentDir.isDirectory()) {
            throw new IOException("The parent directory '" + FileUtil.getCanonicalPath(parentDir) + "' does not exist");
        }

        final File dir = new File(parentDir, String.valueOf(Thread.currentThread().getId()));
        dir.mkdir();

        FileUtils.cleanDirectory(dir);

        return dir;
    }

//    public static File createUniqueTestDir(final File parentDir) throws IOException {
//        if (!parentDir.isDirectory()) {
//            throw new IOException("The parent directory '" + FileUtil.getCanonicalPath(parentDir) + "' does not exist");
//        }
//
//        File dir = null;
//        for (int i = 0; i < 100; i++) {
//            dir = new File(parentDir, FORMAT.format(ZonedDateTime.now(ZoneOffset.UTC)));
//            if (dir.mkdir()) {
//                break;
//            } else {
//                dir = null;
//                ThreadUtil.sleep(100);
//            }
//        }
//
//        if (dir == null) {
//            throw new IOException("Unable to create unique test dir in: " + FileUtil.getCanonicalPath(parentDir));
//        }
//
//        return dir;
//    }

    public static Path createUniqueTestDir(final Path parentDir) throws IOException {
        if (!Files.isDirectory(parentDir)) {
            throw new IOException("The parent directory '" + parentDir.toAbsolutePath().toString() + "' does not exist");
        }

        Path dir = null;
        for (int i = 0; i < 100; i++) {
            dir = parentDir.resolve(FORMAT.format(ZonedDateTime.now(ZoneOffset.UTC)));
            try {
                Files.createDirectories(dir);
                break;
            } catch (final IOException e) {
                dir = null;
                ThreadUtil.sleep(100);
            }
        }

        if (dir == null) {
            throw new IOException("Unable to create unique test dir in: " + parentDir.toAbsolutePath().toString());
        }

        return dir;
    }
//
//    public static void destroyTestDir(final File testDir) {
//        try {
//            FileUtils.deleteDirectory(testDir);
//        } catch (final IOException e) {
//            // Ignore
//        }
//    }
}
