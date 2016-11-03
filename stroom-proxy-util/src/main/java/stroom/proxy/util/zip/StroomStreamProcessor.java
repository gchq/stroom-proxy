package stroom.proxy.util.zip;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.util.StringUtils;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import stroom.proxy.util.cert.CertificateUtil;
import stroom.proxy.util.date.DateUtil;
import stroom.proxy.util.io.ByteCountInputStream;
import stroom.proxy.util.io.CloseableUtil;
import stroom.proxy.util.io.InitialByteArrayOutputStream;
import stroom.proxy.util.io.InitialByteArrayOutputStream.BufferPos;
import stroom.proxy.util.io.StreamProgressMonitor;
import stroom.proxy.util.io.StreamUtil;
import stroom.proxy.util.logging.StroomLogger;

public class StroomStreamProcessor {
    private static StroomLogger LOGGER = StroomLogger.getLogger(StroomStreamProcessor.class);

    private final static String ZERO_CONTENT = "0";

    private final HeaderMap globalHeaderMap;
    private final List<? extends StroomStreamHandler> stroomStreamHandlerList;
    private final byte[] buffer;
    private StreamProgressMonitor streamProgressMonitor = new StreamProgressMonitor("StroomStreamProcessor ");
    private static String hostName;
    private boolean appendReceivedPath = true;

    @SuppressWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
    public StroomStreamProcessor(final HeaderMap headerMap, final List<? extends StroomStreamHandler> stroomStreamHandlerList,
                                 final byte[] buffer, final String logPrefix) {
        this.globalHeaderMap = headerMap;
        this.buffer = buffer;
        this.stroomStreamHandlerList = stroomStreamHandlerList;
    }

    public static void setHostName(final String hostName) {
        StroomStreamProcessor.hostName = hostName;
    }

    public String getHostName() {
        if (hostName == null) {
            try {
                setHostName(InetAddress.getLocalHost().getHostName());
            } catch (final Exception ex) {
                setHostName("Unknown");
            }
        }
        return hostName;
    }

    public void setAppendReceivedPath(final boolean appendReceivedPath) {
        this.appendReceivedPath = appendReceivedPath;
    }

    public void setStreamProgressMonitor(final StreamProgressMonitor streamProgressMonitor) {
        this.streamProgressMonitor = streamProgressMonitor;
    }

    public void processRequestHeader(final HttpServletRequest httpServletRequest) {
        String guid = globalHeaderMap.get(StroomHeaderArguments.GUID);

        // Allocate a GUID if we have not got one.
        if (guid == null) {
            guid = UUID.randomUUID().toString();
            globalHeaderMap.put(StroomHeaderArguments.GUID, guid);

            // Only allocate RemoteXxx details if the GUID has not been
            // allocated.

            // Allocate remote address if not set.
            if (StringUtils.hasText(httpServletRequest.getRemoteAddr())) {
                globalHeaderMap.put(StroomHeaderArguments.REMOTE_ADDRESS, httpServletRequest.getRemoteAddr());
            }

            // Save the time the data was received.
            globalHeaderMap.put(StroomHeaderArguments.RECEIVED_TIME, DateUtil.createNormalDateTimeString());

            // Allocate remote address if not set.
            if (StringUtils.hasText(httpServletRequest.getRemoteHost())) {
                globalHeaderMap.put(StroomHeaderArguments.REMOTE_HOST, httpServletRequest.getRemoteHost());
            }

            if (httpServletRequest.getAttribute(CertificateUtil.SERVLET_CERT_ARG) != null) {
                // Here we pull out the SSL client certificate and check that it
                // is OK based on the settings of the group the feed belongs to.
                try {
                    final Object[] certs = (Object[]) httpServletRequest.getAttribute(CertificateUtil.SERVLET_CERT_ARG);

                    final X509Certificate cert = CertificateUtil.extractCertificate(certs);
                    final String dn = CertificateUtil.extractDNFromCertificate(cert);
                    final Long expiryDate = CertificateUtil.extractExpiryDateFromCertificate(cert);
                    if (expiryDate != null) {
                        globalHeaderMap.put(StroomHeaderArguments.REMOTE_CERT_EXPIRY,
                                DateUtil.createNormalDateTimeString(expiryDate));
                    }

                    globalHeaderMap.put(StroomHeaderArguments.REMOTE_DN, dn);
                } catch (final Exception ex) {
                    LOGGER.error("doPost() - Failed to extract certificate", ex);
                }
            }
        }

    }

    /**
     * @param inputStream
     * @param prefix
     */
    public void process(InputStream inputStream, final String prefix) {
        try {
            handleHeader();

            boolean compressed = false;

            String compression = globalHeaderMap.get(StroomHeaderArguments.COMPRESSION);

            if (StringUtils.hasText(compression)) {
                compression = compression.toUpperCase(StreamUtil.DEFAULT_LOCALE);
                if (!StroomHeaderArguments.VALID_COMPRESSION_SET.contains(compression)) {
                    throw new StroomStreamException(StroomStatusCode.UNKNOWN_COMPRESSION, compression);
                }
            }

            if (ZERO_CONTENT.equals(globalHeaderMap.get(StroomHeaderArguments.CONTENT_LENGTH))) {
                LOGGER.warn("process() - Skipping Zero Content " + globalHeaderMap);
                return;
            }

            if (StroomHeaderArguments.COMPRESSION_ZIP.equals(compression)) {
                // Handle a zip stream.
                processZipStream(inputStream, prefix);

            } else {
                if (StroomHeaderArguments.COMPRESSION_GZIP.equals(compression)) {
                    // We have to wrap our stream reading code in a individual
                    // try/catch so we can return to the client an error in the
                    // case of a corrupt stream.
                    try {
                        // Use the APACHE GZIP de-compressor as it handles
                        // nested compressed streams
                        inputStream = new GzipCompressorInputStream(inputStream, true);
                        compressed = true;
                    } catch (final IOException ioEx) {
                        throw new StroomStreamException(StroomStatusCode.COMPRESSED_STREAM_INVALID, ioEx.getMessage());
                    }
                }

                handleEntryStart(StroomZipFile.SINGLE_DATA_ENTRY);
                int read = 0;
                long totalRead = 0;

                while (true) {
                    // We have to wrap our stream reading code in a individual
                    // try/catch so we can return to the client an error in the
                    // case of a corrupt stream.
                    try {
                        read = StreamUtil.eagerRead(inputStream, buffer);
                    } catch (final IOException ioEx) {
                        if (compressed == true) {
                            throw new StroomStreamException(StroomStatusCode.COMPRESSED_STREAM_INVALID, ioEx.getMessage());
                        } else {
                            throw ioEx;
                        }
                    }
                    if (read == -1) {
                        break;
                    }
                    streamProgressMonitor.progress(read);
                    handleEntryData(buffer, 0, read);
                    totalRead += read;
                }
                handleEntryEnd();
                final HeaderMap entryHeaderMap = globalHeaderMap.cloneAllowable();
                entryHeaderMap.put(StroomHeaderArguments.STREAM_SIZE, String.valueOf(totalRead));
                sendHeader(StroomZipFile.SINGLE_META_ENTRY, entryHeaderMap);
            }
        } catch (final IOException zex) {
            StroomStreamException.create(zex);
        } finally {
            CloseableUtil.closeLogAndIngoreException(inputStream);
        }

    }

    private void processZipStream(final InputStream inputStream, final String prefix) throws IOException {
        final ByteCountInputStream byteCountInputStream = new ByteCountInputStream(inputStream);

        final Map<String, HeaderMap> bufferedHeaderMap = new HashMap<>();
        final Map<String, Long> dataStreamSizeMap = new HashMap<>();
        final List<String> sendDataList = new ArrayList<>();
        final StroomZipNameSet stroomZipNameSet = new StroomZipNameSet(false);

        final ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(byteCountInputStream);

        ZipArchiveEntry zipEntry = null;
        while (true) {
            // We have to wrap our stream reading code in a individual try/catch
            // so we can return to the client an error in the case of a corrupt
            // stream.
            try {
                zipEntry = zipArchiveInputStream.getNextZipEntry();
            } catch (final IOException ioEx) {
                throw new StroomStreamException(StroomStatusCode.COMPRESSED_STREAM_INVALID, ioEx.getMessage());
            }

            if (zipEntry == null) {
                // All done
                break;
            }

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("process() - " + zipEntry);
            }

            final String entryName = prefix + zipEntry.getName();
            final StroomZipEntry stroomZipEntry = stroomZipNameSet.add(entryName);

            if (StroomZipFileType.Meta.equals(stroomZipEntry.getStroomZipFileType())) {
                final HeaderMap entryHeaderMap = globalHeaderMap.cloneAllowable();
                // We have to wrap our stream reading code in a individual
                // try/catch so we can return to the client an error in the case
                // of a corrupt stream.
                try {
                    entryHeaderMap.read(zipArchiveInputStream, false);
                } catch (final IOException ioEx) {
                    throw new StroomStreamException(StroomStatusCode.COMPRESSED_STREAM_INVALID, ioEx.getMessage());
                }

                if (appendReceivedPath) {
                    // Here we build up a list of stroom servers that have received
                    // the message

                    // The entry one will be initially set at the boundary Stroom
                    // server
                    final String entryReceivedServer = entryHeaderMap.get(StroomHeaderArguments.RECEIVED_PATH);

                    if (entryReceivedServer != null) {
                        if (!entryReceivedServer.contains(getHostName())) {
                            entryHeaderMap.put(StroomHeaderArguments.RECEIVED_PATH,
                                    entryReceivedServer + "," + getHostName());
                        }
                    } else {
                        entryHeaderMap.put(StroomHeaderArguments.RECEIVED_PATH, getHostName());
                    }
                }

                if (entryHeaderMap.containsKey(StroomHeaderArguments.STREAM_SIZE)) {
                    // Header already has stream size so just send it on
                    sendHeader(stroomZipEntry, entryHeaderMap);
                } else {
                    // We need to add the stream size
                    // Send the data file yet ?
                    final String dataFile = stroomZipNameSet.getName(stroomZipEntry.getBaseName(), StroomZipFileType.Data);
                    if (dataFile != null && dataStreamSizeMap.containsKey(dataFile)) {
                        // Yes we can send the header now
                        entryHeaderMap.put(StroomHeaderArguments.STREAM_SIZE,
                                String.valueOf(dataStreamSizeMap.get(dataFile)));
                        sendHeader(stroomZipEntry, entryHeaderMap);
                    } else {
                        // Else we have to buffer it
                        bufferedHeaderMap.put(stroomZipEntry.getBaseName(), entryHeaderMap);
                    }
                }
            } else {
                handleEntryStart(stroomZipEntry);
                long totalRead = 0;
                int read = 0;
                while (true) {
                    // We have to wrap our stream reading code in a individual
                    // try/catch so we can return to the client an error in the
                    // case of a corrupt stream.
                    try {
                        read = StreamUtil.eagerRead(zipArchiveInputStream, buffer);
                    } catch (final IOException ioEx) {
                        throw new StroomStreamException(StroomStatusCode.COMPRESSED_STREAM_INVALID, ioEx.getMessage());
                    }
                    if (read == -1) {
                        break;
                    }
                    streamProgressMonitor.progress(read);
                    handleEntryData(buffer, 0, read);
                    totalRead += read;
                }
                handleEntryEnd();

                if (StroomZipFileType.Data.equals(stroomZipEntry.getStroomZipFileType())) {
                    sendDataList.add(entryName);
                    dataStreamSizeMap.put(entryName, totalRead);
                }

                // Buffered header can now be sent as we have sent the
                // data
                if (stroomZipEntry.getBaseName() != null) {
                    final HeaderMap entryHeaderMap = bufferedHeaderMap.remove(stroomZipEntry.getBaseName());
                    if (entryHeaderMap != null) {
                        entryHeaderMap.put(StroomHeaderArguments.STREAM_SIZE, String.valueOf(totalRead));
                        handleEntryStart(new StroomZipEntry(null, stroomZipEntry.getBaseName(), StroomZipFileType.Meta));
                        final byte[] headerBytes = entryHeaderMap.toByteArray();
                        handleEntryData(headerBytes, 0, headerBytes.length);
                        handleEntryEnd();
                    }
                }
            }

        }

        if (stroomZipNameSet.getBaseNameSet().isEmpty()) {
            if (byteCountInputStream.getByteCount() > 22) {
                throw new StroomStreamException(StroomStatusCode.COMPRESSED_STREAM_INVALID, "No Zip Entries");
            } else {
                LOGGER.warn("processZipStream() - Zip stream with no entries ! %s", globalHeaderMap);
            }
        }

        // Add missing headers
        for (final String baseName : stroomZipNameSet.getBaseNameList()) {
            final String headerName = stroomZipNameSet.getName(baseName, StroomZipFileType.Meta);
            // Send Generic Header
            if (headerName == null) {
                final String dataFileName = stroomZipNameSet.getName(baseName, StroomZipFileType.Data);
                final HeaderMap entryHeaderMap = globalHeaderMap.cloneAllowable();
                entryHeaderMap.put(StroomHeaderArguments.STREAM_SIZE,
                        String.valueOf(dataStreamSizeMap.remove(dataFileName)));
                sendHeader(new StroomZipEntry(null, baseName, StroomZipFileType.Meta), entryHeaderMap);
            }
        }
    }

    public void closeHandlers() {
        for (final StroomStreamHandler handler : stroomStreamHandlerList) {
            if (handler instanceof Closeable) {
                CloseableUtil.closeLogAndIngoreException((Closeable) handler);
            }
        }
    }

    private void sendHeader(final StroomZipEntry stroomZipEntry, final HeaderMap headerMap) throws IOException {
        handleEntryStart(stroomZipEntry);
        // Try and use the buffer
        final InitialByteArrayOutputStream byteArrayOutputStream = new InitialByteArrayOutputStream(buffer);
        headerMap.write(byteArrayOutputStream, true);
        final BufferPos bufferPos = byteArrayOutputStream.getBufferPos();
        handleEntryData(bufferPos.getBuffer(), 0, bufferPos.getBufferPos());
        handleEntryEnd();
    }

    private void handleHeader() throws IOException {
        for (final StroomStreamHandler stroomStreamHandler : stroomStreamHandlerList) {
            if (stroomStreamHandler instanceof StroomHeaderStreamHandler) {
                ((StroomHeaderStreamHandler) stroomStreamHandler).handleHeader(globalHeaderMap);
            }
        }
    }

    private void handleEntryStart(final StroomZipEntry stroomZipEntry) throws IOException {
        for (final StroomStreamHandler stroomStreamHandler : stroomStreamHandlerList) {
            stroomStreamHandler.handleEntryStart(stroomZipEntry);
        }
    }

    private void handleEntryEnd() throws IOException {
        for (final StroomStreamHandler stroomStreamHandler : stroomStreamHandlerList) {
            stroomStreamHandler.handleEntryEnd();
        }
    }

    private void handleEntryData(final byte[] data, final int off, final int len) throws IOException {
        for (final StroomStreamHandler stroomStreamHandler : stroomStreamHandlerList) {
            stroomStreamHandler.handleEntryData(data, off, len);
        }
    }
}
