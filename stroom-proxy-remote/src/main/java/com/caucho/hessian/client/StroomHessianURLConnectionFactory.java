package com.caucho.hessian.client;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

public class StroomHessianURLConnectionFactory implements HessianConnectionFactory {
    private static final Logger log = Logger.getLogger(HessianURLConnectionFactory.class.getName());

    private HessianProxyFactory _proxyFactory;
    private final boolean ignoreSSLHostnameVerifier;

    public StroomHessianURLConnectionFactory(final boolean ignoreSSLHostnameVerifier) {
        this.ignoreSSLHostnameVerifier = ignoreSSLHostnameVerifier;
    }

    @Override
    public void setHessianProxyFactory(final HessianProxyFactory factory) {
        _proxyFactory = factory;
    }

    /**
     * Opens a new or recycled connection to the HTTP server.
     */
    @Override
    public HessianConnection open(final URL url) throws IOException {
        if (log.isLoggable(Level.FINER))
            log.finer(this + " open(" + url + ")");

        final URLConnection conn = url.openConnection();

        if (ignoreSSLHostnameVerifier) {
            if (conn instanceof HttpsURLConnection) {
                ((HttpsURLConnection) conn).setHostnameVerifier((hostname, session) -> true);
            }
        }

        final long connectTimeout = _proxyFactory.getConnectTimeout();

        if (connectTimeout >= 0)
            conn.setConnectTimeout((int) connectTimeout);

        conn.setDoOutput(true);

        final long readTimeout = _proxyFactory.getReadTimeout();

        if (readTimeout > 0) {
            try {
                conn.setReadTimeout((int) readTimeout);
            } catch (final Throwable e) {
            }
        }

        return new HessianURLConnection(url, conn);
    }
}
