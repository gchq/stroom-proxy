package stroom.proxy.util.task;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import stroom.proxy.util.shared.Monitor;
import stroom.proxy.util.test.StroomJUnit4ClassRunner;

@RunWith(StroomJUnit4ClassRunner.class)
public class TestMonitorImpl {
    @Test
    public void test_toString() {
        final MonitorImpl root = new MonitorImpl();
        root.info("root");
        final MonitorImpl child1 = new MonitorImpl(root);
        child1.info("child1");
        final MonitorImpl child1child1 = new MonitorImpl(child1);
        child1child1.info("child1child1");
        final MonitorImpl child1child2 = new MonitorImpl(child1);
        child1child2.info("child1child2");
        child1child2.info("child1child2");
        final MonitorImpl child2 = new MonitorImpl(root);
        child2.info("child2");

        final List<Monitor> list = new ArrayList<Monitor>();
        list.add(root);
        list.add(child1);
        list.add(child1child1);
        list.add(child1child2);
        list.add(child2);

        final String tree = MonitorInfoUtil.getInfo(list);

        System.out.println(tree);
    }
}
