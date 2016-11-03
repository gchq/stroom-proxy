package stroom.proxy.util.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import stroom.proxy.util.io.FileUtil;
import stroom.proxy.util.thread.ThreadUtil;

public class StroomTestUtil {
    private static final DateTimeFormatter FORMAT = DateTimeFormat.forPattern("yyyyMMdd_HHmmss_SSS")
            .withZone(DateTimeZone.UTC);

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

    public static File createUniqueTestDir(final File parentDir) throws IOException {
        if (!parentDir.isDirectory()) {
            throw new IOException("The parent directory '" + FileUtil.getCanonicalPath(parentDir) + "' does not exist");
        }

        File dir = null;
        for (int i = 0; i < 100; i++) {
            dir = new File(parentDir, FORMAT.print(System.currentTimeMillis()));
            if (dir.mkdir()) {
                break;
            } else {
                dir = null;
                ThreadUtil.sleep(100);
            }
        }

        if (dir == null) {
            throw new IOException("Unable to create unique test dir in: " + FileUtil.getCanonicalPath(parentDir));
        }

        return dir;
    }

    public static void destroyTestDir(final File testDir) {
        try {
            FileUtils.deleteDirectory(testDir);
        } catch (final IOException e) {
            // Ignore
        }
    }
}
