package stroom.proxy.util.spring;

import java.io.IOException;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import stroom.proxy.util.SystemPropertyUtil;
import stroom.proxy.util.io.StreamUtil;
import stroom.proxy.util.logging.StroomLogger;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

@Component
public class ConfigurableEhCacheManagerFactoryBean
        implements FactoryBean<CacheManager>, InitializingBean, DisposableBean {
    private static StroomLogger LOGGER = StroomLogger.getLogger(ConfigurableEhCacheManagerFactoryBean.class);

    private org.springframework.core.io.Resource configLocation = new ClassPathResource(
            "META-INF/ehcache/stroomCoreServerEhCache.xml");
    private boolean shared;
    private String cacheManagerName = "core-server-cache";
    private CacheManager cacheManager;

    @Resource
    private PropertyConfigurer propertyProvider;

    @Override
    public void afterPropertiesSet() throws IOException, CacheException {
        final String cacheManagerName = generateCacheManagerName();
        cacheManager = CacheManager.getCacheManager(cacheManagerName);
        if (cacheManager == null) {
            try {
                final String template = StreamUtil.streamToString(configLocation.getInputStream());
                final String configString = SystemPropertyUtil.replaceProperty(template, propertyProvider);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Using Cache Config " + configString);
                }

                configLocation = new ByteArrayResource(configString.getBytes(StreamUtil.DEFAULT_CHARSET));

            } catch (final Exception ex) {
                throw new RuntimeException(ex);
            }

            LOGGER.info("Initializing EHCache CacheManager - " + cacheManagerName);
            Configuration configuration = null;
            if (configLocation != null) {
                configuration = ConfigurationFactory.parseConfiguration(configLocation.getInputStream());
            } else {
                configuration = ConfigurationFactory.parseConfiguration();
            }
            configuration.setName(cacheManagerName);

            cacheManager = CacheManager.newInstance(configuration);

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("afterPropertiesSet()");
                final String[] names = cacheManager.getCacheNames();

                for (final String name : names) {
                    LOGGER.debug("afterPropertiesSet() - Cache Name " + name);
                }
            }
        }
    }

    private String generateCacheManagerName() {
        if (shared) {
            return cacheManagerName;
        }

        if (cacheManagerName == null) {
            return UUID.randomUUID().toString();
        }

        return cacheManagerName + ":" + UUID.randomUUID().toString();
    }

    @Override
    public CacheManager getObject() {
        return cacheManager;
    }

    @Override
    public Class<? extends CacheManager> getObjectType() {
        return (cacheManager != null ? cacheManager.getClass() : CacheManager.class);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void destroy() {
        LOGGER.info("Shutting down EHCache CacheManager - " + cacheManager.getName());
        cacheManager.shutdown();
    }

    /**
     * Set the location of the EHCache config file. A typical value is
     * "/WEB-INF/ehcache.xml".
     * <p>
     * Default is "ehcache.xml" in the root of the class path, or if not found,
     * "ehcache-failsafe.xml" in the EHCache jar (default EHCache
     * initialization).
     *
     * @see net.sf.ehcache.CacheManager#create(java.io.InputStream)
     * @see net.sf.ehcache.CacheManager#CacheManager(java.io.InputStream)
     */
    public void setConfigLocation(final org.springframework.core.io.Resource configLocation) {
        this.configLocation = configLocation;
    }

    /**
     * Set whether the EHCache CacheManager should be shared (as a singleton at
     * the VM level) or independent (typically local within the application).
     * Default is "false", creating an independent instance.
     *
     * @see net.sf.ehcache.CacheManager#create()
     * @see net.sf.ehcache.CacheManager#CacheManager()
     */
    public void setShared(final boolean shared) {
        this.shared = shared;
    }

    /**
     * Set the name of the EHCache CacheManager (if a specific name is desired).
     *
     * @see net.sf.ehcache.CacheManager#setName(String)
     */
    public void setCacheManagerName(final String cacheManagerName) {
        this.cacheManagerName = cacheManagerName;
    }

    public void setPropertyProvider(final PropertyConfigurer propertyProvider) {
        this.propertyProvider = propertyProvider;
    }
}
