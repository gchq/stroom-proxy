package stroom.proxy.util.io;

import java.io.Closeable;
import java.io.IOException;

import stroom.proxy.util.logging.StroomLogger;

public class CloseableUtil {
    static StroomLogger LOGGER = StroomLogger.getLogger(CloseableUtil.class);

    public static void closeLogAndIngoreException(Closeable... closeableList) {
        try {
            close(closeableList);
        } catch (Exception ex) {
            // Already Logged
        }
    }

    public static void close(Closeable... closeableList) throws IOException {
        Exception lastException = null;
        if (closeableList != null) {
            for (Closeable closeable : closeableList) {
                if (closeable != null) {
                    try {
                        closeable.close();
                    } catch (Exception e) {
                        lastException = e;
                        LOGGER.error("closeStream()", e);
                    }
                }
            }
        }
        if (lastException != null) {
            if (lastException instanceof RuntimeException) {
                throw (RuntimeException) lastException;
            } else {
                if (lastException instanceof IOException) {
                    throw (IOException) lastException;
                }
                throw new RuntimeException(lastException);
            }
        }
    }
}
