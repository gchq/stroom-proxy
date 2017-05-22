package stroom.proxy.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.feed.MetaMap;
import stroom.proxy.repo.ProxyRepositoryManager;
import stroom.proxy.repo.StroomZipEntry;
import stroom.proxy.repo.StroomZipOutputStream;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Factory to return back handlers for incoming and outgoing requests.
 */
public class ProxyRepositoryRequestHandler implements RequestHandler {
    private static Logger LOGGER = LoggerFactory.getLogger(ProxyRepositoryRequestHandler.class);
    @Resource
    ProxyRepositoryManager proxyRepositoryManager;
    @Resource
    MetaMap metaMap;

    private StroomZipOutputStream stroomZipOutputStream;
    private OutputStream entryStream;
    private boolean doneOne = false;

    public ProxyRepositoryRequestHandler() {
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
            stroomZipOutputStream.addMissingMetaMap(metaMap);
            stroomZipOutputStream.close();
        } else {
            stroomZipOutputStream.closeDelete();
        }
        stroomZipOutputStream = null;
    }

    @Override
    public void handleHeader() throws IOException {
        stroomZipOutputStream = proxyRepositoryManager.getActiveRepository().getStroomZipOutputStream(metaMap);
    }

    @Override
    public void handleError() throws IOException {
        if (stroomZipOutputStream != null) {
            LOGGER.info("Removing part written file {}", stroomZipOutputStream);
            stroomZipOutputStream.closeDelete();
        }
    }

    @Override
    public void validate() {
    }
}