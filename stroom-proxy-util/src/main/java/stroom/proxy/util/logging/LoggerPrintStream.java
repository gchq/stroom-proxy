package stroom.proxy.util.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import stroom.proxy.util.io.StreamUtil;

/**
 * Utility to log messages to log4j from a print writer.
 */
public class LoggerPrintStream extends PrintStream {
    /**
     * This logger can also look out for lines being written in the log.
     */
    private Map<String, AtomicInteger> watchTerms;

    public void addWatchTerm(String watchTerm) {
        if (watchTerms == null) {
            watchTerms = new ConcurrentHashMap<String, AtomicInteger>();
        }
        watchTerms.put(watchTerm, new AtomicInteger(0));
    }

    public int getWatchTermCount(String watchTerm) {
        if (watchTerms != null && watchTerms.containsKey(watchTerm)) {
            return watchTerms.get(watchTerm).get();
        }
        return 0;
    }

    static class LoggerBuffer extends ByteArrayOutputStream {
        LoggerPrintStream parent;

        @Override
        public void flush() {
            try {
                super.flush();
            } catch (IOException e) {
            }
            if (parent != null) {
                parent.doFlush();
            }
        }

        @Override
        public void write(byte[] b) throws IOException {
            super.write(b);
            flush();
        }

        @Override
        public void write(byte[] b, int off, int len) {
            super.write(b, off, len);
            flush();
        }

        @Override
        public synchronized void write(int b) {
            super.write(b);
            flush();
        }
    }

    private StroomLogger logger;
    private boolean debug;
    private LoggerBuffer loggerBuffer;

    public LoggerPrintStream(StroomLogger logger, boolean debug) throws UnsupportedEncodingException {
        this(logger, debug, new LoggerBuffer());
    }

    public LoggerPrintStream(StroomLogger logger) throws UnsupportedEncodingException {
        this(logger, true, new LoggerBuffer());
    }

    public static LoggerPrintStream create(StroomLogger logger, boolean debug) {
        try {
            return new LoggerPrintStream(logger, debug);
        } catch (UnsupportedEncodingException useEx) {
            throw new RuntimeException(useEx);
        }
    }

    public static LoggerPrintStream create(StroomLogger logger) {
        try {
            return new LoggerPrintStream(logger);
        } catch (UnsupportedEncodingException useEx) {
            throw new RuntimeException(useEx);
        }
    }

    private LoggerPrintStream(StroomLogger logger, boolean debug, LoggerBuffer os) throws UnsupportedEncodingException {
        super(os, false, StreamUtil.DEFAULT_CHARSET_NAME);
        this.logger = logger;
        this.debug = debug;
        this.loggerBuffer = os;
        this.loggerBuffer.parent = this;
    }

    // kill the return chars and write to log4j
    void doFlush() {
        if (logger != null && loggerBuffer != null) {
            if ((logger.isDebugEnabled() && debug) || logger.isInfoEnabled() && !debug) {
                String logLine = new String(loggerBuffer.toByteArray(), StreamUtil.DEFAULT_CHARSET);
                if (logLine.endsWith("\n")) {
                    logLine = logLine.substring(0, logLine.length() - 1);
                }
                if (logLine.length() > 0) {
                    // Any watch Terms
                    if (watchTerms != null) {
                        for (Entry<String, AtomicInteger> watchTerm : watchTerms.entrySet()) {
                            int startIndex = -1;
                            while ((startIndex = logLine.indexOf(watchTerm.getKey(), startIndex + 1)) != -1) {
                                watchTerm.getValue().incrementAndGet();
                            }
                        }
                    }
                    if (debug) {
                        logger.debug(logLine);
                    } else {
                        logger.info(logLine);
                    }
                }
            }
            loggerBuffer.reset();
        }
    }

}
