package stroom.proxy.util.io;

import java.io.IOException;
import java.io.InputStream;

public class ByteCountInputStream extends WrappedInputStream {
    private long byteCount;

    public ByteCountInputStream(InputStream inputStream) {
        super(inputStream);
    }

    public long getByteCount() {
        return byteCount;
    }

    @Override
    public int read() throws IOException {
        int r = super.read();
        if (r >= 0) {
            byteCount++;
        }
        return r;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int r = super.read(b);
        if (r >= 0) {
            byteCount += r;
        }
        return r;

    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int r = super.read(b, off, len);
        if (r >= 0) {
            byteCount += r;
        }
        return r;
    }

}
