/*
 * Copyright 2017 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package stroom.proxy.repo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stroom.proxy.handler.StroomStreamHandler;
import stroom.proxy.util.io.CloseableUtil;
import stroom.proxy.util.io.InitialByteArrayOutputStream;
import stroom.proxy.util.io.InitialByteArrayOutputStream.BufferPos;
import stroom.proxy.util.io.StreamProgressMonitor;
import stroom.proxy.util.shared.ModelStringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Class that reads a nested directory tree of stroom zip files.
 */
public abstract class StroomZipRepositoryProcessor extends RepositoryProcessor {
    public final static int DEFAULT_MAX_AGGREGATION = 10000;
    public final static long DEFAULT_MAX_STREAM_SIZE = ModelStringUtil.parseIECByteSizeString("10G");
    private final Logger LOGGER = LoggerFactory.getLogger(StroomZipRepositoryProcessor.class);

    /**
     * The max number of parts to send in a zip file
     */
    private int maxAggregation = DEFAULT_MAX_AGGREGATION;
    /**
     * The max size of the stream before giving up on this iteration
     */
    private Long maxStreamSize = DEFAULT_MAX_STREAM_SIZE;

    public StroomZipRepositoryProcessor(final Monitor monitor) {
        super(monitor);
    }

    public abstract void processFeedFiles(StroomZipRepository stroomZipRepository, String feed, List<Path> fileList);

    public abstract byte[] getReadBuffer();

    public abstract void startExecutor();

    public abstract void stopExecutor(boolean now);

    public abstract void waitForComplete();

    public abstract void execute(String message, Runnable runnable);

    public Long processFeedFile(final List<? extends stroom.proxy.handler.StroomStreamHandler> stroomStreamHandlerList,
                                final StroomZipRepository stroomZipRepository, final Path file,
                                final StreamProgressMonitor streamProgress,
                                final long startSequence) throws IOException {
        long entrySequence = startSequence;
        StroomZipFile stroomZipFile = null;
        boolean bad = true;

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("processFeedFile() - " + file);
        }

        try {
            stroomZipFile = new StroomZipFile(file);

            for (final String sourceName : stroomZipFile.getStroomZipNameSet().getBaseNameSet()) {
                bad = false;

                final String targetName = StroomFileNameUtil.getIdPath(entrySequence++);

                sendEntry(stroomStreamHandlerList, stroomZipFile, sourceName, streamProgress,
                        new StroomZipEntry(null, targetName, StroomZipFileType.Meta));
                sendEntry(stroomStreamHandlerList, stroomZipFile, sourceName, streamProgress,
                        new StroomZipEntry(null, targetName, StroomZipFileType.Context));
                sendEntry(stroomStreamHandlerList, stroomZipFile, sourceName, streamProgress,
                        new StroomZipEntry(null, targetName, StroomZipFileType.Data));
            }
        } catch (final IOException io) {
            stroomZipRepository.addErrorMessage(stroomZipFile, io.getMessage(), bad);
            throw io;
        } finally {
            CloseableUtil.close(stroomZipFile);
        }
        return entrySequence;
    }

    public void setMaxStreamSizeString(final String maxStreamSizeString) {
        this.maxStreamSize = ModelStringUtil.parseIECByteSizeString(maxStreamSizeString);
    }

    public Long getMaxStreamSize() {
        return maxStreamSize;
    }

    public void setMaxStreamSize(final Long maxStreamSize) {
        this.maxStreamSize = maxStreamSize;
    }

    protected void sendEntry(final List<? extends stroom.proxy.handler.StroomStreamHandler> requestHandlerList, final StroomZipFile stroomZipFile,
                             final String sourceName, final StreamProgressMonitor streamProgress,
                             final StroomZipEntry targetEntry)
            throws IOException {
        final InputStream inputStream = stroomZipFile.getInputStream(sourceName, targetEntry.getStroomZipFileType());
        sendEntry(requestHandlerList, inputStream, streamProgress, targetEntry);
    }

    public void sendEntry(final List<? extends stroom.proxy.handler.StroomStreamHandler> stroomStreamHandlerList, final InputStream inputStream,
                          final StreamProgressMonitor streamProgress, final StroomZipEntry targetEntry)
            throws IOException {
        if (inputStream != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("sendEntry() - " + targetEntry);
            }
            final byte[] data = getReadBuffer();
            for (final stroom.proxy.handler.StroomStreamHandler stroomStreamHandler : stroomStreamHandlerList) {
                stroomStreamHandler.handleEntryStart(targetEntry);
            }
            int read;
            long totalRead = 0;
            while ((read = inputStream.read(data)) != -1) {
                totalRead += read;
                streamProgress.progress(read);
                for (final stroom.proxy.handler.StroomStreamHandler stroomStreamHandler : stroomStreamHandlerList) {
                    stroomStreamHandler.handleEntryData(data, 0, read);
                }
            }
            for (final stroom.proxy.handler.StroomStreamHandler stroomStreamHandler : stroomStreamHandlerList) {
                stroomStreamHandler.handleEntryEnd();
            }

            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("sendEntry() - " + targetEntry + " " + ModelStringUtil.formatIECByteSizeString(totalRead));
            }
            if (totalRead == 0) {
                LOGGER.warn("sendEntry() - " + targetEntry + " IS BLANK");
            }
            LOGGER.debug("{} size is {}", targetEntry, totalRead);

        }
    }

    public void sendEntry(final List<? extends stroom.proxy.handler.StroomStreamHandler> stroomStreamHandlerList, final HeaderMap headerMap,
                          final StroomZipEntry targetEntry) throws IOException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("sendEntry() - " + targetEntry);
        }
        final byte[] data = getReadBuffer();
        for (final stroom.proxy.handler.StroomStreamHandler stroomStreamHandler : stroomStreamHandlerList) {
            if (stroomStreamHandler instanceof stroom.proxy.handler.StroomHeaderStreamHandler) {
                ((stroom.proxy.handler.StroomHeaderStreamHandler) stroomStreamHandler).handleHeader(headerMap);
            }
            stroomStreamHandler.handleEntryStart(targetEntry);
        }
        final InitialByteArrayOutputStream initialByteArrayOutputStream = new InitialByteArrayOutputStream(data);
        headerMap.write(initialByteArrayOutputStream, false);
        final BufferPos bufferPos = initialByteArrayOutputStream.getBufferPos();
        for (final stroom.proxy.handler.StroomStreamHandler stroomStreamHandler : stroomStreamHandlerList) {
            stroomStreamHandler.handleEntryData(bufferPos.getBuffer(), 0, bufferPos.getBufferPos());
        }
        for (final StroomStreamHandler stroomStreamHandler : stroomStreamHandlerList) {
            stroomStreamHandler.handleEntryEnd();
        }
    }

    protected void deleteFiles(final StroomZipRepository stroomZipRepository, final List<Path> fileList) {
        for (final Path file : fileList) {
            stroomZipRepository.delete(new StroomZipFile(file));
        }
    }

    public int getMaxAggregation() {
        return maxAggregation;
    }

    public void setMaxAggregation(final int maxAggregation) {
        this.maxAggregation = maxAggregation;
    }
}
