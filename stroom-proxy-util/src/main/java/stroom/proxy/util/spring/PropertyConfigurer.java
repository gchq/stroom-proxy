package stroom.proxy.util.spring;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import stroom.proxy.util.web.ServletContextUtil;

/**
 * Our own PropertyPlaceholderConfigurer that allows access to the properties.
 */
@Component("propertyFileProvider")
public class PropertyConfigurer extends PropertyPlaceholderConfigurer
        implements PropertyProvider, ApplicationContextAware, ResourceLoaderAware {
    private Properties properties;
    private Properties defaultProperties;
    private static Properties overrideProperties;
    private ResourceLoader resourceLoader;
    private Resource[] locations;
    private static volatile String webAppPropertiesName = null;

    public static void setWebAppPropertiesName(String webAppProperties) {
        PropertyConfigurer.webAppPropertiesName = webAppProperties;
    }

    @Override
    public Properties mergeProperties() throws IOException {
        properties = super.mergeProperties();
        if (overrideProperties != null) {
            properties.putAll(overrideProperties);
        }
        return properties;
    }

    /**
     * Hook to add properties.
     */
    public static void setOverrideProperties(Properties overrideProperties) {
        PropertyConfigurer.overrideProperties = overrideProperties;
    }

    @Override
    public void setPropertiesArray(Properties[] propertiesArray) {
        if (propertiesArray != null && propertiesArray.length == 1) {
            defaultProperties = propertiesArray[0];

        }
        super.setPropertiesArray(propertiesArray);
    }

    @Override
    public void setProperties(Properties properties) {
        defaultProperties = properties;
        super.setProperties(properties);
    }

    public Properties getProperties() {
        return properties;
    }

    public Properties getDefaultProperties() {
        return defaultProperties;
    }

    @Override
    public String getProperty(String name) {
        return getProperties().getProperty(name);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext instanceof WebApplicationContext
                || webAppPropertiesName != null && resourceLoader != null) {
            if (webAppPropertiesName == null) {
                setWebAppPropertiesName(ServletContextUtil
                        .getWARName(((WebApplicationContext) applicationContext).getServletContext()));
            }
            // Use the WAR name in preference to the default properties
            Resource warResource = resourceLoader.getResource("classpath:/" + webAppPropertiesName + ".properties");
            if (warResource.exists() && warResource.isReadable()) {
                setLocations(new Resource[] { warResource });
            }
        }
    }

    @Override
    public void setLocation(Resource location) {
        if (locations == null) {
            this.locations = new Resource[] { location };
        }
        setLocations(this.locations);
    }

    @Override
    public void setLocations(Resource[] newLocations) {
        this.locations = Arrays.copyOf(newLocations, newLocations.length);
        super.setLocations(newLocations);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
