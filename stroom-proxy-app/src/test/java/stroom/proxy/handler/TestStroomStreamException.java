package stroom.proxy.handler;

import java.io.IOException;
import java.util.zip.ZipException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import stroom.proxy.StroomStatusCode;
import stroom.proxy.util.test.StroomJUnit4ClassRunner;

@RunWith(StroomJUnit4ClassRunner.class)
public class TestStroomStreamException {
    @Test
    public void testCompressedStreamCorrupt() {
        doTest(new ZipException("test"), StroomStatusCode.COMPRESSED_STREAM_INVALID, "test");
        doTest(new RuntimeException(new ZipException("test")), StroomStatusCode.COMPRESSED_STREAM_INVALID, "test");
        doTest(new RuntimeException(new RuntimeException(new ZipException("test"))),
                StroomStatusCode.COMPRESSED_STREAM_INVALID, "test");
        doTest(new IOException(new ZipException("test")), StroomStatusCode.COMPRESSED_STREAM_INVALID, "test");
    }

    @Test
    public void testOtherError() {
        doTest(new RuntimeException("test"), StroomStatusCode.UNKNOWN_ERROR, "test");
    }

    private void doTest(Exception exception, StroomStatusCode stroomStatusCode, String msg) {
        try {
            StroomStreamException.create(exception);
            Assert.fail();
        } catch (StroomStreamException stroomStreamExcpetion) {
            Assert.assertEquals(
                    "Stroom Status " + stroomStatusCode.getCode() + " - " + stroomStatusCode.getMessage() + " - " + msg,
                    stroomStreamExcpetion.getMessage());
        }
    }

}
