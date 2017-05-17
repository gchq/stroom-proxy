package stroom.proxy.repo;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

public interface StroomZipOutputStream extends Closeable, AutoCloseable {
    long getProgressSize();

    OutputStream addEntry(StroomZipEntry entry) throws IOException;

    long getEntryCount();

    void addMissingMetaMap(HeaderMap headerMap) throws IOException;

    void closeDelete() throws IOException;
}