package stroom.proxy.util.zip;

import java.io.IOException;
import java.io.OutputStream;

import stroom.proxy.util.io.CloseableUtil;

public class StroomZipOutputStreamUtil {
    public static void addSimpleEntry(StroomZipOutputStream stroomZipOutputStream, StroomZipEntry entry, byte[] data)
            throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = stroomZipOutputStream.addEntry(entry);
            outputStream.write(data);
        } finally {
            CloseableUtil.close(outputStream);
        }
    }

    public static StroomStreamHandler createStroomStreamHandler(final StroomZipOutputStream stroomZipOutputStream) {
        return new StroomStreamHandler() {
            private OutputStream outputStream;

            @Override
            public void handleEntryStart(StroomZipEntry stroomZipEntry) throws IOException {
                outputStream = stroomZipOutputStream.addEntry(stroomZipEntry);
            }

            @Override
            public void handleEntryEnd() throws IOException {
                CloseableUtil.close(outputStream);
            }

            @Override
            public void handleEntryData(byte[] data, int off, int len) throws IOException {
                if (outputStream != null) {
                    outputStream.write(data, off, len);
                }
            }
        };
    }

    public static StroomStreamHandler createStroomStreamOrderCheck() {
        final StroomZipNameSet stroomZipNameSet = new StroomZipNameSet(true);
        return new StroomStreamHandler() {
            @Override
            public void handleEntryStart(StroomZipEntry stroomZipEntry) throws IOException {
                stroomZipNameSet.add(stroomZipEntry.getFullName());
            }

            @Override
            public void handleEntryEnd() throws IOException {
            }

            @Override
            public void handleEntryData(byte[] data, int off, int len) throws IOException {
            }
        };
    }
}
