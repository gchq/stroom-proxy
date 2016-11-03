package stroom.proxy.util.web;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

public class ServletContextUtil {
    private static final String DEFAULT_NAME = "stroom";
    private static final String WEBAPP = "webapp";

    public final static String getWARName(ServletConfig servletConfig) {
        if (servletConfig == null) {
            return DEFAULT_NAME;
        }

        return getWARName(servletConfig.getServletContext());
    }

    public final static String getWARName(ServletContext servletContext) {
        final String fullPath = servletContext.getRealPath(".");
        final String[] parts = fullPath.split("/");

        if (WEBAPP.equals(parts[parts.length - 1])) {
            return DEFAULT_NAME;
        }

        return parts[parts.length - 2];
    }
}
