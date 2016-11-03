package stroom.proxy.util.logging;

import org.junit.Test;
import org.junit.runner.RunWith;

import stroom.proxy.util.test.StroomExpectedException;
import stroom.proxy.util.test.StroomJUnit4ClassRunner;

@RunWith(StroomJUnit4ClassRunner.class)
public class TestStroomJUnit4ClassRunner {
    private static final StroomLogger LOGGER = StroomLogger.getLogger(TestStroomJUnit4ClassRunner.class);

    @Test
    @StroomExpectedException(exception = RuntimeException.class)
    public void testSimple1() {
        LOGGER.error("MSG", new RuntimeException());
    }
}
