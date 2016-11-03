package stroom.proxy.util.test;

import stroom.proxy.util.io.FileUtil;
import org.apache.commons.io.FileUtils;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

@Category(UnitTest.class)
@RunWith(StroomJUnit4ClassRunner.class)
public abstract class StroomUnitTest implements StroomTest {
    @Override
    public File getCurrentTestDir() {
        return FileUtil.getTempDir();
    }

    @Override
    public void clearTestDir() {
        File dir = getCurrentTestDir();
        try {
            FileUtils.cleanDirectory(dir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to clear directory " + dir.getAbsolutePath(), e);
        }
    }
}
