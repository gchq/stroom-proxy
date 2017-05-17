package stroom.proxy.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

class CloseableUtil {
    static Logger LOGGER = LoggerFactory.getLogger(CloseableUtil.class);

    static void closeLogAndIngoreException(Closeable... closeableList) {
        try {
            close(closeableList);
        } catch (Exception ex) {
            // Already Logged
        }
    }

    static void close(Closeable... closeableList) throws IOException {
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
