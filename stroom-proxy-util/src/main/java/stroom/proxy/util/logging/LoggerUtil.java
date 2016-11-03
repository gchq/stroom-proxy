package stroom.proxy.util.logging;

import java.util.IllegalFormatException;

/**
 * Build a message from array argument.
 */
public class LoggerUtil {
    public final static String buildMessage(final Object... args) {
        if (args == null) {
            return "";
        }
        IllegalFormatException ilEx = null;
        try {
            if (args[0] != null && args[0] instanceof String) {
                if (args.length > 1) {
                    final Object[] otherArgs = new Object[args.length - 1];
                    System.arraycopy(args, 1, otherArgs, 0, otherArgs.length);
                    return String.format((String) args[0], otherArgs);
                } else {
                    return (String) args[0];
                }
            }
        } catch (final IllegalFormatException il) {
            ilEx = il;
        }
        final StringBuilder builder = new StringBuilder();
        if (ilEx != null) {
            builder.append(ilEx.getMessage());
        }
        for (final Object arg : args) {
            if (arg != null) {
                if (builder.length() > 0) {
                    builder.append(" - ");
                }
                builder.append(String.valueOf(arg));
            }
        }
        return builder.toString();
    }
}
