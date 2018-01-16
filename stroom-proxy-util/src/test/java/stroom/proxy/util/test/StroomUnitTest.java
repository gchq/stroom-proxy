package stroom.proxy.util.test;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.proxy.util.io.FileUtil;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

@Category(UnitTest.class)
@RunWith(StroomJUnit4ClassRunner.class)
public abstract class StroomUnitTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(StroomUnitTest.class);

    public void clearTestDir() {
        final Path path = getCurrentTestPath();
        try {
            Files.walkFileTree(path, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                    delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(final Path file, final IOException exc) {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(final Path dir, final IOException exc) {
                    if (!path.equals(dir)) {
                        delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Unable to clear directory " + path, e);
        }
    }

    private static void delete(final Path path) {
        try {
            if (path != null) {
                Files.delete(path);
            }
        } catch (IOException e) {
            LOGGER.trace(e.getMessage(), e);
        }
    }

    public Path getCurrentTestPath() {
        return FileUtil.getTempPath();
    }
}
