package stroom.proxy.util.io;

import java.io.IOException;
import java.io.InputStream;

public class WrappedInputStream extends InputStream {
    private final InputStream inputStream;

    public WrappedInputStream(final InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        inputStream.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return inputStream.markSupported();
    }

    @Override
    public int read() throws IOException {
        return inputStream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return inputStream.read(b, off, len);
    }

    @Override
    public int read(byte[] b) throws IOException {
        return inputStream.read(b);
    }

    @Override
    public synchronized void reset() throws IOException {
        inputStream.reset();
    }

    @Override
    public long skip(long n) throws IOException {
        return inputStream.skip(n);
    }

    @Override
    public final boolean equals(Object obj) {
        return inputStream.equals(obj);
    }

    @Override
    public final int hashCode() {
        return inputStream.hashCode();
    }

    @Override
    public String toString() {
        return inputStream.toString();
    }
}
