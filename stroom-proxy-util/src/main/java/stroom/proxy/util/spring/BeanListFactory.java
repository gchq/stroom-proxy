package stroom.proxy.util.spring;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

@Component
public class BeanListFactory {
    @Resource
    private StroomBeanStore stroomBeanStore;

    private List<String> beanList;

    public void setBeanList(List<String> beanList) {
        this.beanList = beanList;
    }

    public List<Object> create() {
        List<Object> list = new ArrayList<Object>();
        if (beanList != null && stroomBeanStore != null) {
            for (String name : beanList) {
                list.add(stroomBeanStore.getBean(name));
            }
        }
        return list;
    }

}
