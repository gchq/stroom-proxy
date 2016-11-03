package stroom.proxy.util.zip;

import stroom.proxy.util.logging.StroomLogger;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

public class StroomZipFile implements Closeable {
    public final static String SINGLE_ENTRY_ZIP_BASE_NAME = "001";

    public final static StroomZipEntry SINGLE_DATA_ENTRY = new StroomZipEntry(null, SINGLE_ENTRY_ZIP_BASE_NAME,
                                                                        StroomZipFileType.Data);
    public final static StroomZipEntry SINGLE_META_ENTRY = new StroomZipEntry(null, SINGLE_ENTRY_ZIP_BASE_NAME,
                                                                        StroomZipFileType.Meta);
    public final static StroomZipEntry SINGLE_CONTEXT_ENTRY = new StroomZipEntry(null, SINGLE_ENTRY_ZIP_BASE_NAME,
                                                                           StroomZipFileType.Context);

    private static StroomLogger LOGGER = StroomLogger.getLogger(StroomZipFile.class);

    private final File file;
    private ZipFile zipFile;
    private RuntimeException openStack;
    private StroomZipNameSet stroomZipNameSet;
    private long totalSize = 0;

    public StroomZipFile() {
        this.file = null;
        this.zipFile = null;
    }

    public StroomZipFile(File file) {
        this.file = file;
        openStack = new RuntimeException();
    }

    private ZipFile getZipFile() throws IOException {
        if (zipFile == null) {
            this.zipFile = new ZipFile(file);
        }
        return zipFile;
    }

    public File getFile() {
        return file;
    }

    public StroomZipNameSet getStroomZipNameSet() throws IOException {
        if (stroomZipNameSet == null) {
            stroomZipNameSet = new StroomZipNameSet(false);
            Enumeration<ZipArchiveEntry> entryE = getZipFile().getEntries();

            while (entryE.hasMoreElements()) {
                ZipArchiveEntry entry = entryE.nextElement();

                // Skip Dir's
                if (!entry.isDirectory()) {
                    String fileName = entry.getName();
                    stroomZipNameSet.add(fileName);
                }

                long entrySize = entry.getSize();
                if (entrySize == -1 || totalSize == -1) {
                    // Can nolonger sum
                } else {
                    totalSize += entrySize;
                }

            }
        }
        return stroomZipNameSet;
    }

    public Long getTotalSize() throws IOException {
        getStroomZipNameSet();
        if (totalSize == -1) {
            return null;
        } else {
            return totalSize;
        }
    }

    @Override
    public void close() throws IOException {
        if (zipFile != null) {
            zipFile.close();
            zipFile = null;
        }
        stroomZipNameSet = null;

    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (zipFile != null) {
            LOGGER.error("finalize() - Failed to close stream opened here", openStack);
        }
    }

    public InputStream getInputStream(String baseName, StroomZipFileType fileType) throws IOException {
        String fullName = getStroomZipNameSet().getName(baseName, fileType);
        if (fullName != null) {
            return getZipFile().getInputStream(getZipFile().getEntry(fullName));
        }
        return null;
    }

    public boolean containsEntry(String baseName, StroomZipFileType fileType) throws IOException {
        String fullName = getStroomZipNameSet().getName(baseName, fileType);
        if (fullName != null) {
            return getZipFile().getEntry(fullName) != null;
        }
        return false;
    }

    public boolean renameTo(File newFileName) throws IOException {
        close();
        return file.renameTo(newFileName);
    }

    public boolean delete() throws IOException {
        close();
        return file.delete();
    }
}
