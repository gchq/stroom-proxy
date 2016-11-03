package stroom.proxy.util.io;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import stroom.proxy.util.test.StroomJUnit4ClassRunner;

@RunWith(StroomJUnit4ClassRunner.class)
public class TestStreamUtil {
    private final static int SIZE = 25;

    private static class TestInputStream extends InputStream {
        int read = 0;

        @Override
        public int read() throws IOException {
            read++;
            if (read > SIZE) {
                return -1;
            }
            return 1;
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, 1);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return super.read(b, off, 1);
        }
    };

    @Test
    public void testFullRead() throws IOException {
        byte[] buffer = new byte[10];

        InputStream testStream = new TestInputStream();
        Assert.assertEquals(10, StreamUtil.eagerRead(testStream, buffer));
        Assert.assertEquals(10, StreamUtil.eagerRead(testStream, buffer));
        Assert.assertEquals(5, StreamUtil.eagerRead(testStream, buffer));
        Assert.assertEquals(-1, StreamUtil.eagerRead(testStream, buffer));
        Assert.assertEquals(-1, StreamUtil.eagerRead(testStream, buffer));
    }

    @Test
    public void testException() {
        try {
            throw new RuntimeException();
        } catch (RuntimeException ex) {
            String callStack = StreamUtil.exceptionCallStack(ex);
            Assert.assertTrue(callStack, callStack.contains("testException"));
        }
    }
}
