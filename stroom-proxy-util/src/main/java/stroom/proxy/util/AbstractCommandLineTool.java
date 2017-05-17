package stroom.proxy.util;

import org.apache.commons.lang.StringUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for command line tools that handles setting a load of args on the
 * program as name value pairs.
 */
public abstract class AbstractCommandLineTool {
    private static class Example extends AbstractCommandLineTool {
        @Override
        public void run() {
            throw new RuntimeException();
        }
    }

    private Map<String, String> map;
    private List<String> validArguments;
    private int maxPropLength = 0;

    public abstract void run();

    protected void checkArgs() {
    }

    protected void failArg(final String arg, final String msg) {
        throw new RuntimeException(arg + " - " + msg);
    }

    public void init(final String[] args) throws Exception {
        validArguments = new ArrayList<>();
        map = loadArgs(args);

        final BeanInfo beanInfo = Introspector.getBeanInfo(this.getClass());

        for (final PropertyDescriptor field : beanInfo.getPropertyDescriptors()) {
            if (field.getWriteMethod() != null) {
                if (field.getName().length() > maxPropLength) {
                    maxPropLength = field.getName().length();
                }
                if (map.containsKey(field.getName())) {
                    validArguments.add(field.getName());
                    field.getWriteMethod().invoke(this, getAsType(field));
                }
            }
        }

        checkArgs();
    }

    private Map<String, String> loadArgs(final String[] args) {
        final Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            final String[] split = args[i].split("=");
            if (split.length > 1) {
                map.put(split[0].toLowerCase(), split[1]);
            } else {
                map.put(split[0].toLowerCase(), "");
            }
        }
        return map;
    }

    private Object getAsType(final PropertyDescriptor descriptor) {
        final Class<?> propertyClass = descriptor.getPropertyType();
        final String propertyName = descriptor.getName().toLowerCase();
        if (propertyClass.equals(String.class)) {
            return map.get(propertyName);
        }
        if (propertyClass.equals(Boolean.class) || propertyClass.equals(Boolean.TYPE)) {
            return Boolean.parseBoolean(map.get(propertyName));
        }
        if (propertyClass.equals(Integer.class) || propertyClass.equals(Integer.TYPE)) {
            return Integer.parseInt(map.get(propertyName));
        }
        if (propertyClass.equals(Long.class) || propertyClass.equals(Long.TYPE)) {
            return Long.parseLong(map.get(propertyName));
        }
        throw new RuntimeException("AbstractCommandLineTool does not know about properties of type " + propertyClass);
    }

    public void doMain(final String[] args) throws Exception {
        init(args);
        run();
    }

    public void traceArguments(final PrintStream printStream) {
        try {
            doTraceArguments(printStream);
        } catch (final Exception ex) {
            printStream.println(ex.getMessage());
        }
    }

    private void doTraceArguments(final PrintStream printStream) throws Exception {
        final BeanInfo beanInfo = Introspector.getBeanInfo(this.getClass());
        for (final PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            // Only do properties with getters
            if (pd.getWriteMethod() != null) {
                // Simple getter ?
                String suffix = " (default)";
                if (map.containsKey(pd.getName().toLowerCase())) {
                    suffix = " (arg)";
                }
                String value = "";

                if (pd.getReadMethod() != null && pd.getReadMethod().getParameterTypes().length == 0) {
                    value = String.valueOf(pd.getReadMethod().invoke(this));
                } else {
                    // No simple getter
                    Field field = null;
                    try {
                        field = this.getClass().getDeclaredField(pd.getName());
                    } catch (final NoSuchFieldException nsfex) {
                        // Ignore
                    }
                    if (field != null) {
                        field.setAccessible(true);
                        value = String.valueOf(field.get(this));
                    } else {
                        value = "?";

                    }
                }
                printStream.println(StringUtils.rightPad(pd.getName(), maxPropLength) + " = " + value + suffix);
            }
        }
    }

    public static void main(final String[] args) throws Exception {
        final Example example = new Example();
        example.doMain(args);
    }

}
