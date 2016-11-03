package stroom.proxy.datafeed;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

import stroom.proxy.util.spring.PropertyConfigurer;
import stroom.proxy.util.test.StroomSpringJUnit4ClassRunner;

@RunWith(StroomSpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:META-INF/spring/stroomProxyCommonHandlerFactoryContext.xml",
        "classpath:META-INF/spring/stroomProxyStoreAndForwardHandlerFactoryContext.xml" })
public class TestProxyHandlerFactoryContext {
    @Resource
    @Qualifier("proxyProperties")
    private PropertyConfigurer proxyProperties;

    @Test
    public void testSpringWire() {
        Assert.assertNotNull(proxyProperties);
    }
}
