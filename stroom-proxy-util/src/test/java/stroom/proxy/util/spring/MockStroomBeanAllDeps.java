package stroom.proxy.util.spring;

import javax.annotation.Resource;

import org.junit.Assert;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(StroomSpringProfiles.TEST)
public class MockStroomBeanAllDeps extends MockStroomBeanLifeCycleBean {
    @Resource
    private MockStroomBeanNoDeps stroomBeanNoDeps;
    @Resource
    private MockStroomBeanSomeDeps stroomBeanSomeDeps;

    @Override
    @StroomStartup
    public void start() {
        Assert.assertTrue(stroomBeanNoDeps.isRunning());
        Assert.assertTrue(stroomBeanSomeDeps.isRunning());

        super.start();
    }

    @Override
    @StroomShutdown(priority = 100)
    public void stop() {
        super.stop();
    }
}
