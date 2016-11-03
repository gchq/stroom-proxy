package stroom.proxy.util.io;

import java.io.IOException;
import java.io.OutputStream;

public class FilterOutputStreamProgressMonitor extends WrappedOutputStream {
    private final StreamProgressMonitor streamProgressMonitor;

    public FilterOutputStreamProgressMonitor(OutputStream outputStream, StreamProgressMonitor streamProgressMonitor) {
        super(outputStream);
        this.streamProgressMonitor = streamProgressMonitor;
    }

    @Override
    public void write(byte[] b) throws IOException {
        super.write(b);
        streamProgressMonitor.progress(b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        streamProgressMonitor.progress(len);
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);
        streamProgressMonitor.progress(1);
    }

}
