package stroom.proxy.remote;

import com.caucho.hessian.client.StroomHessianURLConnectionFactory;
import com.caucho.hessian.client.HessianConnectionFactory;
import com.caucho.hessian.client.HessianProxyFactory;

public class StroomHessianProxyFactory extends HessianProxyFactory {
    private boolean ignoreSSLHostnameVerifier = true;

    @Override
    protected HessianConnectionFactory createHessianConnectionFactory() {
        final String className = System.getProperty(HessianConnectionFactory.class.getName());

        HessianConnectionFactory factory = null;

        try {
            if (className != null) {
                final ClassLoader loader = Thread.currentThread().getContextClassLoader();

                final Class<?> cl = Class.forName(className, false, loader);

                factory = (HessianConnectionFactory) cl.newInstance();

                return factory;
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }

        return new StroomHessianURLConnectionFactory(ignoreSSLHostnameVerifier);
    }

    public void setIgnoreSSLHostnameVerifier(final boolean ignoreSSLHostnameVerifier) {
        this.ignoreSSLHostnameVerifier = ignoreSSLHostnameVerifier;
    }
}
