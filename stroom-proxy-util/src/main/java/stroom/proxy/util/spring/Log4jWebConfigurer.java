package stroom.proxy.util.spring;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.servlet.ServletContext;

import org.springframework.core.io.Resource;
import org.springframework.util.Log4jConfigurer;
import org.springframework.web.util.WebUtils;

public class Log4jWebConfigurer {
    public final static long LOG4J_REFRESH_MS = 10 * 1000;

    /**
     * Initialize log4j, including setting the web app root system property.
     *
     * @param servletContext
     *            the current ServletContext
     * @see WebUtils#setWebAppRootSystemProperty
     */
    public static void initLogging(ServletContext servletContext, Resource resource) {
        // Perform actual log4j initialization; else rely on log4j's default
        // initialization.
        try {
            String path = resource.getFile().getAbsolutePath();

            // Write log message to server log.
            servletContext.log("Initializing log4j from [" + path + "]");

            Log4jConfigurer.initLogging(path, LOG4J_REFRESH_MS);
        } catch (FileNotFoundException ex) {
            throw new IllegalArgumentException("Invalid 'log4jConfigLocation' parameter: " + ex.getMessage());
        } catch (IOException ioEx) {
            throw new IllegalArgumentException("Invalid 'log4jConfigLocation' parameter: " + ioEx.getMessage());
        }
    }

    /**
     * Shut down log4j, properly releasing all file locks and resetting the web
     * app root system property.
     *
     * @param servletContext
     *            the current ServletContext
     * @see WebUtils#removeWebAppRootSystemProperty
     */
    public static void shutdownLogging(ServletContext servletContext) {
        servletContext.log("Shutting down log4j");
        Log4jConfigurer.shutdownLogging();
    }
}
