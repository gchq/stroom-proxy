package stroom.proxy.handler;

import stroom.proxy.repo.ProxyRepositoryManager;
import stroom.proxy.util.logging.StroomLogger;
import stroom.proxy.util.zip.StroomZipEntry;
import stroom.proxy.util.zip.StroomZipOutputStream;
import stroom.proxy.util.zip.HeaderMap;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Factory to return back handlers for incoming and outgoing requests.
 */
public class ProxyRepositoryRequestHandler implements RequestHandler {
    private static StroomLogger LOGGER = StroomLogger.getLogger(ProxyRepositoryRequestHandler.class);
    @Resource
    ProxyRepositoryManager proxyRepositoryManager;
    @Resource
    HeaderMap headerMap;
    OutputStream requestOutputStream;
    private StroomZipOutputStream stroomZipOutputStream;
    private OutputStream entryStream;
    private String zipFilenameDelimiter;
    private String zipFilenameTemplate;
    private boolean doneOne = false;

    public ProxyRepositoryRequestHandler() {
    }

    public void setZipFilenameTemplate(final String zipFilenameTemplate) {
        this.zipFilenameTemplate = zipFilenameTemplate;
    }

    public void setZipFilenameDelimiter(final String zipFilenameDelimiter) {
        this.zipFilenameDelimiter = zipFilenameDelimiter;
    }

    @Override
    public void handleEntryStart(StroomZipEntry stroomZipEntry) throws IOException {
        doneOne = true;
        entryStream = stroomZipOutputStream.addEntry(stroomZipEntry);
    }

    @Override
    public void handleEntryEnd() throws IOException {
        entryStream.close();
        entryStream = null;
    }

    @Override
    public void handleEntryData(byte[] buffer, int off, int len) throws IOException {
        entryStream.write(buffer, off, len);
    }

    @Override
    public void handleFooter() throws IOException {
        if (doneOne) {
            stroomZipOutputStream.addMissingMetaMap(headerMap);
            stroomZipOutputStream.close();
        } else {
            stroomZipOutputStream.closeDelete();
        }
        stroomZipOutputStream = null;
    }

    @Override
    public void handleHeader() throws IOException {
        stroomZipOutputStream = proxyRepositoryManager.getActiveRepository().getStroomZipOutputStream(headerMap,
                                                                                                zipFilenameTemplate);
    }

    @Override
    public void handleError() throws IOException {
        if (stroomZipOutputStream != null) {
            LOGGER.info("handleError() - Removing part written file " + stroomZipOutputStream.getFinalFile());
            stroomZipOutputStream.closeDelete();
        }
    }

    @Override
    public void validate() {
    }


}
