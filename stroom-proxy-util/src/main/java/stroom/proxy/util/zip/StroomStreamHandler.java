package stroom.proxy.util.zip;

import java.io.IOException;

public interface StroomStreamHandler {
    void handleEntryStart(StroomZipEntry stroomZipEntry) throws IOException;

    void handleEntryData(byte[] data, int off, int len) throws IOException;

    void handleEntryEnd() throws IOException;

}
