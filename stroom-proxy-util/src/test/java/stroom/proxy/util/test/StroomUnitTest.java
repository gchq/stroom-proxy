package stroom.proxy.util.test;

import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.proxy.util.io.FileUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

@Category(UnitTest.class)
@RunWith(StroomJUnit4ClassRunner.class)
public abstract class StroomUnitTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(StroomUnitTest.class);

    public void clearTestDir() {
        final Path dir = getCurrentTestPath();
        try (final Stream<Path> stream = Files.walk(dir).filter(p -> !p.equals(dir)).sorted(Comparator.reverseOrder())) {
            stream.forEach(StroomUnitTest::delete);
        } catch (IOException e) {
            throw new RuntimeException("Unable to clear directory " + dir, e);
        }
    }

    private static void delete(final Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            LOGGER.trace(e.getMessage(), e);
        }
    }

    public Path getCurrentTestPath() {
        return FileUtil.getTempPath();
    }
}
