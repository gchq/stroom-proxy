package stroom.proxy.util.spring;

import org.junit.Assert;

public class MockStroomBeanLifeCycleBean {
    boolean running = false;
    boolean hasRun = false;

    @StroomStartup
    public void start() {
        if (running) {
            Assert.fail("Called start twice");
        }
        if (hasRun) {
            Assert.fail("Called start twice");
        }
        hasRun = false;
        running = true;
    }

    @StroomShutdown
    public void stop() {
        if (!running) {
            Assert.fail("Stopped called and not running");
        }
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
