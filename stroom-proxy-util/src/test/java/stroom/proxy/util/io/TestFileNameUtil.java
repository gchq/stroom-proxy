package stroom.proxy.util.io;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import stroom.proxy.util.test.StroomJUnit4ClassRunner;

@RunWith(StroomJUnit4ClassRunner.class)
public class TestFileNameUtil {
    @Test
    public void testSimple() {
        Assert.assertEquals("001", FileNameUtil.getBaseName("001.dat"));
        Assert.assertEquals("001.001", FileNameUtil.getBaseName("001.001.dat"));
        Assert.assertEquals("001", FileNameUtil.getBaseName("001"));
    }
}
