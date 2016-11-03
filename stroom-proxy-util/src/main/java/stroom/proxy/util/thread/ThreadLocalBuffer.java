package stroom.proxy.util.thread;

import org.springframework.util.StringUtils;

/**
 * Buffer held in thread scope for performance reasons.
 */
public class ThreadLocalBuffer {
    /**
     * Same size as JDK's Buffered Output Stream.
     */
    public final static int DEFAULT_BUFFER_SIZE = 8192;

    private String bufferSize = null;

    private ThreadLocal<byte[]> threadLocalBuffer = new ThreadLocal<byte[]>();

    public byte[] getBuffer() {
        byte[] buffer = threadLocalBuffer.get();
        if (buffer == null) {
            buffer = new byte[getDerivedBufferSize()];
            threadLocalBuffer.set(buffer);
        }
        return buffer;
    }

    public void setBufferSize(String bufferSize) {
        this.bufferSize = bufferSize;
    }

    public int getDerivedBufferSize() {
        if (!StringUtils.hasText(bufferSize)) {
            return DEFAULT_BUFFER_SIZE;
        } else {
            return Integer.parseInt(bufferSize);
        }
    }

}
