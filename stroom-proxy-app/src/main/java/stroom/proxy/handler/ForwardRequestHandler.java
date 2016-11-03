package stroom.proxy.handler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.Resource;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.springframework.util.StringUtils;

import stroom.proxy.util.logging.StroomLogger;
import stroom.proxy.util.shared.ModelStringUtil;
import stroom.proxy.util.thread.ThreadUtil;
import stroom.proxy.util.zip.StroomHeaderArguments;
import stroom.proxy.util.zip.StroomStreamException;
import stroom.proxy.util.zip.StroomZipEntry;
import stroom.proxy.util.zip.HeaderMap;

/**
 * Handler class that forwards the request to a URL.
 */
public class ForwardRequestHandler implements RequestHandler, HostnameVerifier {
    private static StroomLogger LOGGER = StroomLogger.getLogger(ForwardRequestHandler.class);

    private Integer forwardTimeoutMs = null;
    private Integer forwardDelayMs = null;
    private Integer forwardChunkSize;

    private String guid = null;
    private HttpURLConnection connection = null;
    private ZipOutputStream zipOutputStream;
    private long startTimeMs;
    private long bytesSent = 0;

    @Resource
    private HeaderMap headerMap;

    @Resource
    private ForwardRequestHandlerUrlFactory forwardRequestHandlerUrlFactory;

    private String forwardUrl;

    public ForwardRequestHandler() {
    }

    @Override
    public void handleEntryStart(StroomZipEntry stroomZipEntry) throws IOException {
        // First call we set up if we are going to do chuncked streaming
        zipOutputStream.putNextEntry(new ZipEntry(stroomZipEntry.getFullName()));
    }

    @Override
    public void handleEntryEnd() throws IOException {
        zipOutputStream.closeEntry();
    }

    /**
     * Handle some pay load.
     */
    @Override
    public void handleEntryData(byte[] buffer, int off, int length) throws IOException {
        bytesSent += length;
        zipOutputStream.write(buffer, off, length);
        if (forwardDelayMs != null) {
            LOGGER.debug("handleEntryData() - adding delay %s", forwardDelayMs);
            ThreadUtil.sleep(forwardDelayMs);
        }
    }

    @Override
    public void handleFooter() throws IOException {
        zipOutputStream.close();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("handleFooter() - header fields " + connection.getHeaderFields());
        }
        int responseCode = -1;

        if (connection != null) {
            try {
                responseCode = StroomStreamException.checkConnectionResponse(connection);
            } finally {
                LOGGER.info("handleFooter() - %s took %s to forward %s response %s - %s", guid,
                        ModelStringUtil.formatDurationString(System.currentTimeMillis() - startTimeMs),
                        ModelStringUtil.formatByteSizeString(bytesSent), responseCode, headerMap);
                connection.disconnect();
                connection = null;
            }
        }

    }

    public String getForwardUrl() {
        if (forwardUrl == null) {
            forwardUrl = forwardRequestHandlerUrlFactory.getForwardUrlPart();
        }
        return forwardUrl;
    }

    @Override
    public void handleHeader() throws IOException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("handleHeader() - " + getForwardUrl() + " Sending request " + headerMap);
        }
        startTimeMs = System.currentTimeMillis();
        guid = headerMap.getOrCreateGuid();

        URL url = new URL(getForwardUrl());
        connection = (HttpURLConnection) url.openConnection();

        sslCheck();

        if (forwardTimeoutMs != null) {
            connection.setConnectTimeout(forwardTimeoutMs);
            connection.setReadTimeout(0);
            // Don't set a read time out else big files will fail
            // connection.setReadTimeout(forwardTimeoutMs);
        }

        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/audit");
        connection.setDoOutput(true);
        connection.setDoInput(true);

        connection.addRequestProperty(StroomHeaderArguments.COMPRESSION, StroomHeaderArguments.COMPRESSION_ZIP);

        HeaderMap sendHeader = headerMap.cloneAllowable();
        for (Entry<String, String> entry : sendHeader.entrySet()) {
            connection.addRequestProperty(entry.getKey(), entry.getValue());
        }

        if (forwardChunkSize != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("handleHeader() - setting ChunkedStreamingMode = " + forwardChunkSize);
            }
            connection.setChunkedStreamingMode(forwardChunkSize);
        }
        connection.connect();
        zipOutputStream = new ZipOutputStream(connection.getOutputStream());
    }

    private void sslCheck() {
        if (connection instanceof HttpsURLConnection) {
            ((HttpsURLConnection) connection).setHostnameVerifier(this);
        }

    }

    @Override
    public boolean verify(String arg0, SSLSession arg1) {
        return true;
    }

    @Override
    public void handleError() throws IOException {
        LOGGER.info("handleError() - " + getForwardUrl());
        if (connection != null) {
            connection.disconnect();
        }
    }

    @Override
    public void validate() {
        try {
            URL url = new URL(getForwardUrl());
            connection = (HttpURLConnection) url.openConnection();
            connection.disconnect();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void setForwardTimeoutMs(String timeout) {
        this.forwardTimeoutMs = getInteger(timeout, forwardTimeoutMs);
    }

    public void setForwardDelayMs(String forwardDelayMs) {
        this.forwardDelayMs = getInteger(forwardDelayMs, this.forwardDelayMs);
    }

    public void setForwardChunkSize(Integer chunkSize) {
        this.forwardChunkSize = chunkSize;
    }

    private Integer getInteger(String newVal, Integer defVal) {
        if (StringUtils.hasText(newVal)) {
            return Integer.valueOf(newVal);
        } else {
            return defVal;
        }
    }

}
