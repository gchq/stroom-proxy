package stroom.proxy.util;

import java.util.Set;

import stroom.proxy.util.spring.PropertyProvider;

public final class SystemPropertyUtil {
    private static class SystemPropertyProvider implements PropertyProvider {
        @Override
        public String getProperty(final String key) {
            String prop = System.getProperty(key);
            if (prop == null) {
                prop = System.getenv(key);
            }
            if (prop == null) {
                throw new RuntimeException("Property or environment variable \"" + key + "\" not found");
            }
            return prop;
        }
    }

    private static final SystemPropertyProvider SYSTEM_PROPERTY_PROVIDER = new SystemPropertyProvider();

    public static final String replaceProperty(final String string, final PropertyProvider provider) {
        return replaceProperty(string, provider, null);
    }

    public static final String replaceProperty(String string, final PropertyProvider provider,
            final Set<String> ignore) {
        if (string != null) {
            int start = 0;
            int end = 0;
            while (start != -1) {
                start = string.indexOf("${", end);
                if (start != -1) {
                    end = string.indexOf("}", start);
                    if (end != -1) {
                        final String name = string.substring(start + 2, end);
                        end++;

                        if (ignore == null || !ignore.contains(name)) {
                            final String prop = provider.getProperty(name);

                            if (prop == null) {
                                throw new RuntimeException("System property not found: " + name);
                            } else {
                                string = string.substring(0, start) + prop + string.substring(end);
                            }
                        }
                    } else {
                        throw new RuntimeException("Invalid environment variable declaration in: " + string);
                    }
                }
            }
        }
        return string;
    }

    public static final String replaceSystemProperty(final String string, final Set<String> ignore) {
        return replaceProperty(string, SYSTEM_PROPERTY_PROVIDER, ignore);
    }
}
