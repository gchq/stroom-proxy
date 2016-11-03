package stroom.proxy.util.io;

import java.io.BufferedOutputStream;
import java.io.OutputStream;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Buffered stream that lets you pass in the buffer.
 */
public class ByteArrayBufferedOutputStream extends BufferedOutputStream {
    @SuppressWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
    public ByteArrayBufferedOutputStream(OutputStream outputStream, byte[] buffer) {
        super(outputStream, 1);
        this.buf = buffer;
    }

}
