package stroom.proxy.handler;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;

import org.springframework.util.StringUtils;

import stroom.proxy.repo.ProxyRepositoryManager;
import stroom.proxy.util.date.DateUtil;
import stroom.proxy.util.io.StreamProgressMonitor;
import stroom.proxy.util.logging.StroomLogger;
import stroom.proxy.util.scheduler.Scheduler;
import stroom.proxy.util.scheduler.SimpleCron;
import stroom.proxy.util.shared.Monitor;
import stroom.proxy.util.spring.StroomShutdown;
import stroom.proxy.util.spring.StroomStartup;
import stroom.proxy.util.task.MonitorImpl;
import stroom.proxy.util.thread.ThreadLocalBuffer;
import stroom.proxy.util.thread.ThreadScopeContextHolder;
import stroom.proxy.util.thread.ThreadUtil;
import stroom.proxy.util.zip.StroomHeaderArguments;
import stroom.proxy.util.zip.StroomZipRepository;
import stroom.proxy.util.zip.StroomZipRepositorySimpleExecutorProcessor;
import stroom.proxy.util.zip.HeaderMap;

/**
 * Class that reads repositories.
 */
public class ProxyRepositoryReader extends StroomZipRepositorySimpleExecutorProcessor implements Runnable {
    private static final StroomLogger LOGGER = StroomLogger.getLogger(ProxyRepositoryReader.class);

    public static final String PROXY_FORWARD_ID = "ProxyForwardId";

    @Resource
    private ProxyRepositoryManager proxyRepositoryManager;
    @Resource(name = "proxyRequestThreadLocalBuffer")
    private ThreadLocalBuffer proxyRequestThreadLocalBuffer;

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    /**
     * Our worker thread
     */
    private volatile Thread readerThread;

    /**
     * CRON trigger - can be null
     */
    private volatile Scheduler scheduler;

    private final AtomicLong proxyForwardId = new AtomicLong(0);

    private volatile Boolean hasOutgoingRequestHandlers = null;

    /**
     * Flag set to stop things
     */
    private final AtomicBoolean finish = new AtomicBoolean(false);

    static int instanceCount = 0;

    private volatile String hostName = null;

    public ProxyRepositoryReader() {
        this(new MonitorImpl());
    }

    public ProxyRepositoryReader(final Monitor monitor) {
        super(monitor);
    }

    public String getHostName() {
        if (hostName == null) {
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (final Exception ex) {
                hostName = "Unknown";
            }
        }
        return hostName;
    }

    private synchronized void startReading() {
        if (readerThread == null) {
            finish.set(false);

            readerThread = new Thread(this, "Repository Reader Thread " + (++instanceCount));
            readerThread.start();
        }
    }

    private synchronized void stopReading() {
        if (readerThread != null) {
            finish.set(true);

            lock.lock();
            try {
                condition.signalAll();
            } finally {
                lock.unlock();
            }

            while (readerThread.isAlive()) {
                LOGGER.info("stopReading() - Waiting for read thread to stop");
                ThreadUtil.sleep(1000);
            }
        }
    }

    /**
     * Main Working Thread - Keep looping until we have been told to finish
     */
    @Override
    public void run() {
        lock.lock();
        try {
            while (!finish.get()) {
                try {
                    condition.await(1, TimeUnit.SECONDS);
                } catch (final InterruptedException e) {
                    LOGGER.error(e, e);
                }

                if (!finish.get()) {
                    // Only do the work if we are not on a timer or our timer
                    // says we should fire.
                    if (scheduler != null && !scheduler.execute()) {
                        continue;
                    }

                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("run() - Cron Match at " + DateUtil.createNormalDateTimeString());
                    }

                    try {
                        doRunWork();
                    } catch (final Throwable ex) {
                        LOGGER.error("run() - Unhandled exception coming out of doRunWork()", ex);
                    }
                }
            }
        } finally {
            lock.unlock();
        }

        LOGGER.info("run() - Completed ... Thread Exit");
    }

    void doRunWork() {
        if (proxyRepositoryManager == null) {
            return;
        }
        final List<StroomZipRepository> readyToProcessList = proxyRepositoryManager.getReadableRepository();

        for (final StroomZipRepository readyToProcess : readyToProcessList) {
            if (finish.get()) {
                return;
            }

            // Only process the thing if we have some outgoing handlers.
            if (hasOutgoingRequestHandlers()) {
                process(readyToProcess);
            }
            // Otherwise just clean.
            readyToProcess.clean();
        }
    }

    public List<RequestHandler> createOutgoingRequestHandlerList() {
        return new ArrayList<>();
    }

    @Override
    public byte[] getReadBuffer() {
        return proxyRequestThreadLocalBuffer.getBuffer();
    }

    /**
     * Send a load of files for the same feed
     */
    @Override
    public void processFeedFiles(final StroomZipRepository stroomZipRepository, final String feed,
                                 final List<File> fileList) {
        final long thisPostId = proxyForwardId.incrementAndGet();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("processFeedFiles() - proxyForwardId " + thisPostId + " " + feed + " file count "
                    + fileList.size());
        }

        final HeaderMap headerMap = new HeaderMap();
        headerMap.put(StroomHeaderArguments.FEED, feed);
        headerMap.put(StroomHeaderArguments.COMPRESSION, StroomHeaderArguments.COMPRESSION_ZIP);
        headerMap.put(StroomHeaderArguments.RECEIVED_PATH, getHostName());
        if (LOGGER.isDebugEnabled()) {
            headerMap.put(PROXY_FORWARD_ID, String.valueOf(thisPostId));
        }

        ThreadScopeContextHolder.getContext().put(HeaderMap.NAME, headerMap);

        final List<RequestHandler> requestHandlerList = createOutgoingRequestHandlerList();

        try {
            // Start the post
            for (final RequestHandler requestHandler : requestHandlerList) {
                requestHandler.handleHeader();
            }

            long sequenceId = 1;
            long batch = 1;

            final StreamProgressMonitor streamProgress = new StreamProgressMonitor("ProxyRepositoryReader " + feed);
            List<File> deleteList = new ArrayList<>();

            Long nextBatchBreak = getMaxStreamSize();

            for (final File file : fileList) {
                // Send no more if told to finish
                if (finish.get()) {
                    LOGGER.info("processFeedFiles() - Quitting early as we have been told to stop");
                    break;
                }
                if (sequenceId > getMaxAggregation()
                        || (nextBatchBreak != null && streamProgress.getTotalBytes() > nextBatchBreak)) {
                    batch++;
                    LOGGER.info("processFeedFiles() - Starting new batch %s as sequence %s > %s or size %s > %s", batch,
                            sequenceId, getMaxAggregation(), streamProgress.getTotalBytes(), nextBatchBreak);

                    sequenceId = 1;

                    if (nextBatchBreak != null) {
                        nextBatchBreak = streamProgress.getTotalBytes() + getMaxStreamSize();
                    }

                    // Start a new batch
                    for (final RequestHandler requestHandler : requestHandlerList) {
                        requestHandler.handleFooter();
                    }
                    deleteFiles(stroomZipRepository, deleteList);
                    deleteList = new ArrayList<>();

                    // Start the post
                    for (final RequestHandler requestHandler : requestHandlerList) {
                        requestHandler.handleHeader();
                    }
                }

                sequenceId = processFeedFile(requestHandlerList, stroomZipRepository, file, streamProgress, sequenceId);

                deleteList.add(file);

            }
            for (final RequestHandler requestHandler : requestHandlerList) {
                requestHandler.handleFooter();
            }

            deleteFiles(stroomZipRepository, deleteList);

        } catch (final IOException ex) {
            LOGGER.warn("processFeedFiles() - Failed to send to feed " + feed + " ( " + String.valueOf(ex) + ")");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("processFeedFiles() - Debug trace " + feed, ex);
            }
            for (final RequestHandler requestHandler : requestHandlerList) {
                try {
                    requestHandler.handleError();
                } catch (final IOException ioEx) {
                    LOGGER.error("fileSend()", ioEx);
                }
            }
        }
    }

    @StroomShutdown
    public void stop() {
        finish.set(true);
        LOGGER.info("stop() - Stopping Executor");
        stopExecutor(true);
        LOGGER.info("stop() - Stopped  Executor");
        LOGGER.info("stop() - Stopping Reader Thread");
        stopReading();
        LOGGER.info("stop() - Stopped  Reader Thread");
    }

    public boolean hasOutgoingRequestHandlers() {
        if (hasOutgoingRequestHandlers == null) {
            final List<RequestHandler> requestHandlerList = createOutgoingRequestHandlerList();
            hasOutgoingRequestHandlers = requestHandlerList != null && requestHandlerList.size() > 0;
        }
        return hasOutgoingRequestHandlers.booleanValue();
    }

    @StroomStartup
    public void start() {
        final List<RequestHandler> requestHandlerList = createOutgoingRequestHandlerList();
        hasOutgoingRequestHandlers = requestHandlerList != null && requestHandlerList.size() > 0;

        startReading();
    }

    public void setForwardThreadCount(final int forwardThreadCount) {
        setThreadCount(forwardThreadCount);
    }

    public void setSimpleCron(final String simpleCron) {
        if (StringUtils.hasText(simpleCron)) {
            scheduler = SimpleCron.compile(simpleCron).createScheduler();
        } else {
            scheduler = null;
        }
    }

    public void setProxyRequestThreadLocalBuffer(final ThreadLocalBuffer proxyRequestThreadLocalBuffer) {
        this.proxyRequestThreadLocalBuffer = proxyRequestThreadLocalBuffer;
    }

    public void setProxyRepositoryManager(final ProxyRepositoryManager proxyRepositoryManager) {
        this.proxyRepositoryManager = proxyRepositoryManager;
    }
}
