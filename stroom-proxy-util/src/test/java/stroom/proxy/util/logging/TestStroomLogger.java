package stroom.proxy.util.logging;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import stroom.proxy.util.test.StroomExpectedException;
import stroom.proxy.util.test.StroomJUnit4ClassRunner;
import stroom.proxy.util.thread.ThreadUtil;

@RunWith(StroomJUnit4ClassRunner.class)
public class TestStroomLogger {
    @Test
    public void testSimple() {
        final StroomLogger stroomLogger = StroomLogger.getLogger(TestStroomLogger.class);
        stroomLogger.debug("testSimple() %s", "start");
        Assert.assertEquals("test one", stroomLogger.buildMessage("test %s", "one", "two"));
        Assert.assertEquals("test one two", stroomLogger.buildMessage("test %s %s", "one", "two"));
        Assert.assertEquals("test one two", stroomLogger.buildMessage("test %s %s", "one", "two", new Throwable()));
        Assert.assertNotNull(stroomLogger.extractThrowable("test %s %s", "one", "two", new Throwable()));
        stroomLogger.debug("testSimple() %s", "stop");
    }

    @Test
    public void testNumbers() {
        final StroomLogger stroomLogger = StroomLogger.getLogger(TestStroomLogger.class);
        Assert.assertEquals("1/2", stroomLogger.buildMessage("%s/%s", 1, 2));
    }

    @Test
    public void testLogExecutionTime() {
        final LogExecutionTime logExecutionTime = new LogExecutionTime();
        final StroomLogger stroomLogger = StroomLogger.getLogger(TestStroomLogger.class);
        Assert.assertTrue(stroomLogger.buildMessage("%s", logExecutionTime).contains("ms"));
    }

    @Test
    public void testInterval() {
        final StroomLogger stroomLogger = StroomLogger.getLogger(TestStroomLogger.class);
        stroomLogger.setInterval(1);
        Assert.assertTrue(stroomLogger.checkInterval());
        Assert.assertTrue(ThreadUtil.sleep(100));
        Assert.assertTrue(stroomLogger.checkInterval());
    }

    private String produceMessage(final String level) {
        return "this is my big " + level + " msg";
    }

    @Test
    @StroomExpectedException(exception = Throwable.class)
    public void testMessageSupplier() {
        final StroomLogger stroomLogger = StroomLogger.getLogger(TestStroomLogger.class);

        stroomLogger.trace(() -> produceMessage("trace"));
        stroomLogger.trace(() -> produceMessage("trace"), new Throwable());
        stroomLogger.debug(() -> produceMessage("debug"));
        stroomLogger.debug(() -> produceMessage("debug"), new Throwable());
        stroomLogger.warn(() -> produceMessage("warn"));
        stroomLogger.warn(() -> produceMessage("warn"), new Throwable());
        stroomLogger.error(() -> produceMessage("error"));
        stroomLogger.error(() -> produceMessage("error"), new Throwable());
        stroomLogger.fatal(() -> produceMessage("fatal"));
        stroomLogger.fatal(() -> produceMessage("fatal"), new Throwable());

    }

    @Test
    public void testIfTraceIsEnabled() {
        final StroomLogger stroomLogger = StroomLogger.getLogger(TestStroomLogger.class);

        final AtomicInteger counter = new AtomicInteger(0);

        stroomLogger.ifTraceIsEnabled(() -> counter.incrementAndGet());

        Assert.assertEquals(stroomLogger.isTraceEnabled() ? 1 : 0, counter.get());
    }

    @Test
    public void testIfDebugIsEnabled() {
        final StroomLogger stroomLogger = StroomLogger.getLogger(TestStroomLogger.class);

        final AtomicInteger counter = new AtomicInteger(0);

        stroomLogger.ifDebugIsEnabled(() -> counter.incrementAndGet());

        Assert.assertEquals(stroomLogger.isDebugEnabled() ? 1 : 0, counter.get());
    }

}
