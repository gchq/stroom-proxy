package stroom.proxy.util.spring;

import javax.annotation.Resource;

import org.junit.Assert;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(StroomSpringProfiles.TEST)
public class MockStroomBeanSomeDeps extends MockStroomBeanLifeCycleBean {
    @Resource
    private MockStroomBeanNoDeps stroomBeanNoDeps;

    @Override
    @StroomStartup(priority = 99)
    public void start() {
        Assert.assertTrue(stroomBeanNoDeps.isRunning());

        super.start();
    }

    @Override
    @StroomShutdown(priority = 99)
    public void stop() {
        super.stop();
    }
}
