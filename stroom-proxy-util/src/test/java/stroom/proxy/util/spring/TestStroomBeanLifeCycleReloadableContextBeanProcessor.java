package stroom.proxy.util.spring;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import stroom.proxy.util.test.ComponentTest;

/**
 * This test class does not use a @Configuration because it needs to create
 * multiple contexts manually.
 */
@Category(ComponentTest.class)
public class TestStroomBeanLifeCycleReloadableContextBeanProcessor {
    @Test
    public void testStartAndStop() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(
                StroomBeanLifeCycleTestConfiguration.class)) {
            MockStroomBeanLifeCycleBean bean1 = (MockStroomBeanLifeCycleBean) context.getBean("bean1");
            MockStroomBeanLifeCycleBean bean2 = (MockStroomBeanLifeCycleBean) context.getBean("bean2");

            Assert.assertTrue(bean1.isRunning());
            Assert.assertTrue(bean2.isRunning());

            context.start();
            context.stop();
            context.destroy();

            Assert.assertFalse(bean1.isRunning());
            Assert.assertFalse(bean2.isRunning());
        }
    }

    @Test
    public void testStartAndStopWithTwoContexts() {
        try (AnnotationConfigApplicationContext context1 = new AnnotationConfigApplicationContext(
                StroomBeanLifeCycleTestConfiguration.class);
             AnnotationConfigApplicationContext context2 = new AnnotationConfigApplicationContext(
                        StroomBeanLifeCycleTestConfiguration.class)) {
            MockStroomBeanLifeCycleBean context1Bean1 = (MockStroomBeanLifeCycleBean) context1.getBean("bean1");
            MockStroomBeanLifeCycleBean context1Bean2 = (MockStroomBeanLifeCycleBean) context1.getBean("bean2");

            MockStroomBeanLifeCycleBean context2Bean1 = (MockStroomBeanLifeCycleBean) context2.getBean("bean1");
            MockStroomBeanLifeCycleBean context2Bean2 = (MockStroomBeanLifeCycleBean) context2.getBean("bean2");

            Assert.assertTrue(context1Bean1.isRunning());
            Assert.assertTrue(context1Bean2.isRunning());

            Assert.assertFalse(context2Bean1.isRunning());
            Assert.assertFalse(context2Bean2.isRunning());

            context1.stop();
            context1.destroy();

            Assert.assertFalse(context1Bean1.isRunning());
            Assert.assertFalse(context1Bean2.isRunning());

            Assert.assertTrue(context2Bean1.isRunning());
            Assert.assertTrue(context2Bean2.isRunning());

            context2.stop();
            context2.destroy();

            Assert.assertFalse(context1Bean1.isRunning());
            Assert.assertFalse(context1Bean2.isRunning());

            Assert.assertFalse(context2Bean1.isRunning());
            Assert.assertFalse(context2Bean2.isRunning());
        }
    }

}
