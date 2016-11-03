package stroom.proxy.util.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import stroom.proxy.util.logging.StroomLogger;

/**
 * Package-private context configuration, for use by specific tests.
 */
@Configuration
class StroomBeanLifeCycleTestConfiguration {
    private static final StroomLogger LOGGER = StroomLogger.getLogger(StroomBeanLifeCycleTestConfiguration.class);

    public StroomBeanLifeCycleTestConfiguration() {
        LOGGER.info("StroomBeanLifeCycleConfiguration loading...");
    }

    @Bean
    public MockStroomBeanLifeCycleBean bean1() {
        return new MockStroomBeanLifeCycleBean();
    }

    @Bean
    public MockStroomBeanLifeCycleBean bean2() {
        return new MockStroomBeanLifeCycleBean();
    }

    @Bean
    public StroomBeanLifeCycle stroomBeanLifeCycle() {
        return new StroomBeanLifeCycle();
    }

    @Bean
    StroomBeanStore stroomBeanStore() {
        return new StroomBeanStore();
    }

    @Bean
    public StroomBeanLifeCycleReloadableContextBeanProcessor beanProcessor(StroomBeanLifeCycle stroomBeanLifeCycle) {
        StroomBeanLifeCycleReloadableContextBeanProcessor beanProcessor = new StroomBeanLifeCycleReloadableContextBeanProcessor();
        beanProcessor.setName("testContext");
        beanProcessor.setStroomBeanLifeCycle(stroomBeanLifeCycle);
        return beanProcessor;
    }
}
