package stroom.proxy.util.io;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import stroom.proxy.util.concurrent.SimpleExecutor;
import stroom.proxy.util.test.StroomUnitTest;
import stroom.proxy.util.test.StroomJUnit4ClassRunner;

@RunWith(StroomJUnit4ClassRunner.class)
public class TestFileUtil extends StroomUnitTest {
    @Test
    public void testMkdirs() throws IOException {
        final String tempDir = getCurrentTestDir().getCanonicalPath();
        final String rootDir = tempDir + "/TestFileUtil_" + System.currentTimeMillis();

        final String[] dirArray = new String[10];
        for (int i = 0; i < dirArray.length; i++) {
            dirArray[i] = buildDir(rootDir);
        }
        final AtomicBoolean exception = new AtomicBoolean(false);

        final SimpleExecutor simpleExecutor = new SimpleExecutor(4);
        for (int i = 0; i < 200; i++) {
            final int count = i;
            simpleExecutor.execute(() -> {
                try {
                    final String dir = dirArray[count % dirArray.length];
                    System.out.println(dir);
                    FileUtil.mkdirs(new File(dir));
                } catch (final Exception ex) {
                    ex.printStackTrace();
                    exception.set(true);
                }
            });
        }
        simpleExecutor.waitForComplete();
        simpleExecutor.stop(false);

        Assert.assertEquals(false, exception.get());

        FileUtils.deleteDirectory(new File(rootDir));
    }

    private String buildDir(final String rootDir) {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(rootDir);
        for (int i = 0; i < 10; i++) {
            stringBuilder.append("/");
            stringBuilder.append(RandomUtils.nextInt(10));
        }
        final String dirToCreate = stringBuilder.toString();
        return dirToCreate;
    }

    @Test
    public void testMkdirsUnableToCreate() {
        try {
            FileUtil.mkdirs(new File("/dev/null"));
            Assert.fail("Not expecting that this directory can be created");
        } catch (final Exception ex) {
        }
    }
}
