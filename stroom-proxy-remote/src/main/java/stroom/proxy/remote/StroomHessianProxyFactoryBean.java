package stroom.proxy.remote;

import org.springframework.remoting.caucho.HessianProxyFactoryBean;

public class StroomHessianProxyFactoryBean extends HessianProxyFactoryBean {
    public StroomHessianProxyFactoryBean() {
        setProxyFactory(new StroomHessianProxyFactory());
    }

}
