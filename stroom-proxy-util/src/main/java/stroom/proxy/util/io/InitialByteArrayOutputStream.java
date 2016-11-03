package stroom.proxy.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * An output stream buffer that tries to use a given buffer first and once that
 * is not big enough it delegates all calls to a real ByteArrayOutputStream.
 *
 * It also allows access to the buffer and pos without a copy of the array.
 */
public class InitialByteArrayOutputStream extends OutputStream {
    public static class BufferPos {
        private byte[] buffer;
        private int bufferPos;

        @SuppressWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
        public BufferPos(byte[] buffer, int bufferPos) {
            this.buffer = buffer;
            this.bufferPos = bufferPos;
        }

        @SuppressWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
        public byte[] getBuffer() {
            return buffer;
        }

        public int getBufferPos() {
            return bufferPos;
        }

        @Override
        public String toString() {
            return new String(buffer, 0, bufferPos, StreamUtil.DEFAULT_CHARSET);
        }
    }

    private byte[] preBuffer;
    private int preBufferPos = 0;
    private GetBufferByteArrayOutputStream postBuffer;

    private static class GetBufferByteArrayOutputStream extends ByteArrayOutputStream {
        @SuppressWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
        public byte[] getBuffer() {
            return buf;
        }
    }

    @SuppressWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
    public InitialByteArrayOutputStream(byte[] initialBuffer) {
        preBuffer = initialBuffer;
    }

    public BufferPos getBufferPos() {
        if (postBuffer == null) {
            return new BufferPos(preBuffer, preBufferPos);
        } else {
            return new BufferPos(postBuffer.getBuffer(), postBuffer.size());
        }
    }

    @Override
    public void write(int b) throws IOException {
        checkPreBufferSize(1);

        if (postBuffer != null) {
            postBuffer.write(b);
        } else {
            preBuffer[preBufferPos] = (byte) b;
            preBufferPos++;
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        checkPreBufferSize(b.length);

        if (postBuffer != null) {
            postBuffer.write(b);
        } else {
            System.arraycopy(b, 0, preBuffer, preBufferPos, b.length);
            preBufferPos = preBufferPos + b.length;
        }

    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        checkPreBufferSize(len);

        if (postBuffer != null) {
            postBuffer.write(b, off, len);
        } else {
            System.arraycopy(b, off, preBuffer, preBufferPos, len);
            preBufferPos = preBufferPos + len;
        }
    }

    private void checkPreBufferSize(int add) {
        // Not using a post buffer
        if (postBuffer == null) {
            // Exceeded our initial buffer?
            if (preBufferPos + add > preBuffer.length) {
                // Flush our pre buffer
                postBuffer = new GetBufferByteArrayOutputStream();
                postBuffer.write(preBuffer, 0, preBufferPos);
            }
        }
    }

}
