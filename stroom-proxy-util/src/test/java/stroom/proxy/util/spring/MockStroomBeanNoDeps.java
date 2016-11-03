package stroom.proxy.util.spring;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile(StroomSpringProfiles.TEST)
public class MockStroomBeanNoDeps extends MockStroomBeanLifeCycleBean {
    @Override
    @StroomStartup(priority = 100)
    public void start() {
        super.start();
    }

    @Override
    @StroomShutdown
    public void stop() {
        super.stop();
    }
}
