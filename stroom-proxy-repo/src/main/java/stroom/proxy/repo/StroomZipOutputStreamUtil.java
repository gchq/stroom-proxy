package stroom.proxy.repo;

import java.io.IOException;
import java.io.OutputStream;

class StroomZipOutputStreamUtil {
    static void addSimpleEntry(StroomZipOutputStream stroomZipOutputStream, StroomZipEntry entry, byte[] data)
            throws IOException {
        OutputStream outputStream = null;
        try {
            outputStream = stroomZipOutputStream.addEntry(entry);
            outputStream.write(data);
        } finally {
            CloseableUtil.close(outputStream);
        }
    }
}
