package stroom.proxy.repo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;

import stroom.proxy.util.date.DateUtil;
import stroom.proxy.util.io.FileNameUtil;
import stroom.proxy.util.io.FileUtil;
import stroom.proxy.util.logging.StroomLogger;
import stroom.proxy.util.scheduler.Scheduler;
import stroom.proxy.util.scheduler.SimpleCron;
import stroom.proxy.util.spring.StroomShutdown;
import stroom.proxy.util.spring.StroomStartup;
import stroom.proxy.util.thread.ThreadUtil;
import stroom.proxy.util.zip.StroomZipRepository;

/**
 * Manager class that handles rolling the repository if required. Also tracks
 * old rolled repositories.
 */
public class ProxyRepositoryManager implements Runnable {
    private static final StroomLogger LOGGER = StroomLogger.getLogger(ProxyRepositoryManager.class);

    private final AtomicReference<StroomZipRepository> activeRepository = new AtomicReference<>();
    private final List<StroomZipRepository> rolledRepository = new ArrayList<>();

    private volatile Scheduler scheduler;
    private volatile Thread timerThread;
    private volatile boolean finish = false;

    private volatile File rootRepoDir = null;
    private volatile int lockDeleteAgeMs = 1000 * 60 * 60;

    private String zipFilenameDelimiter;

    @StroomStartup(priority = 100)
    public synchronized void start() {
        scanForOldRepositories();

        // Force the active one to be created
        getActiveRepository();

        // Rolling?
        if (scheduler != null) {
            timerThread = new Thread(this);
            timerThread.start();
        }
    }

    @Required
    public void setRepoDir(final String dir) {
        if (StringUtils.hasText(dir)) {
            rootRepoDir = new File(dir);
        } else {
            rootRepoDir = new File(FileUtil.getTempDir(), "stroom-proxy");
            LOGGER.warn("setRepoDir() - Using temp dir as repoDir is not set. " + rootRepoDir);
        }
    }

    public void scanForOldRepositories() {
        final String[] files = rootRepoDir.list();
        if (files != null) {
            for (final String fileName : files) {
                final File file = new File(rootRepoDir, fileName);
                if (file.isDirectory()) {
                    final String baseName = FileNameUtil.getBaseName(fileName);
                    // Looks like a date
                    if (baseName.length() == DateUtil.DATE_LENGTH) {
                        try {
                            // Is this DIR an Zulu time stamp ?
                            DateUtil.parseNormalDateTimeString(baseName);
                            // YES looking like a repository
                            final File expectedDir = new File(rootRepoDir, baseName);

                            if (fileName.endsWith(StroomZipRepository.LOCK_EXTENSION)) {
                                if (!file.renameTo(expectedDir)) {
                                    LOGGER.warn(
                                            "scanForOldRepositories() - Failed to rename locked repository " + file);
                                } else {
                                    LOGGER.info("scanForOldRepositories() - Unlocking old locked repository "
                                            + expectedDir);
                                    rolledRepository.add(new StroomZipRepository(expectedDir.getAbsolutePath(), false,
                                            lockDeleteAgeMs, zipFilenameDelimiter));
                                }
                            } else {
                                LOGGER.info(
                                        "scanForOldRepositories() - Picking up old rolled repository " + expectedDir);
                                rolledRepository.add(
                                        new StroomZipRepository(expectedDir.getAbsolutePath(), false, lockDeleteAgeMs, zipFilenameDelimiter));
                            }
                        } catch (final IllegalArgumentException pex) {
                            LOGGER.warn(
                                    "scanForOldRepositories() - Failed to parse directory that looked like it should be rolled repository "
                                            + file);
                        }
                    }
                }
            }
        }
    }

    public StroomZipRepository getActiveRepository() {
        if (activeRepository.get() == null) {
            synchronized (ProxyRepositoryManager.class) {
                if (activeRepository.get() == null) {
                    if (scheduler == null) {
                        // Open a static repository
                        activeRepository
                                .set(new StroomZipRepository(rootRepoDir.getAbsolutePath(), false, lockDeleteAgeMs, zipFilenameDelimiter));
                    } else {
                        final String dir = rootRepoDir.getAbsolutePath() + "/"
                                + DateUtil.createFileDateTimeString(System.currentTimeMillis());
                        // Open a rolling repository
                        activeRepository.set(new StroomZipRepository(dir, true, lockDeleteAgeMs, zipFilenameDelimiter));
                    }
                }
            }
        }
        return activeRepository.get();
    }

    public List<StroomZipRepository> getReadableRepository() {
        final List<StroomZipRepository> rtnList = new ArrayList<StroomZipRepository>();
        if (rolledRepository != null) {
            rtnList.addAll(rolledRepository);
        }
        if (scheduler == null && activeRepository.get() != null) {
            rtnList.add(activeRepository.get());
        }
        return rtnList;
    }

    public synchronized void removeIfEmpty(final StroomZipRepository repository) {
        if (repository != activeRepository.get()) {
            if (repository.deleteIfEmpty()) {
                rolledRepository.remove(repository);
            }
        }
    }

    @Override
    public void run() {
        while (!finish) {
            // Sleep for a second
            ThreadUtil.sleep(1000);

            try {
                doRunWork();
            } catch (final Throwable th) {
                LOGGER.error("run() Exception", th);
            }
        }
    }

    void doRunWork() {
        if (scheduler != null && scheduler.execute()) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("run() - Cron Match at " + DateUtil.createNormalDateTimeString());
            }
            // Create a new one
            // Do this with a lock on this object so no one can call finish
            synchronized (this) {
                if (activeRepository != null) {
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info("run() rolling repository");
                    }
                    // Swap them
                    final StroomZipRepository lastActiveProxyRepository = activeRepository.getAndSet(null);
                    // Tell the last one to finish
                    lastActiveProxyRepository.finish();
                    rolledRepository.add(lastActiveProxyRepository);
                }
            }
        }
    }

    public void setLockDeleteAgeMs(final int lockDeleteAgeMs) {
        this.lockDeleteAgeMs = lockDeleteAgeMs;
    }

    public void setSimpleCron(final String simpleCron) {
        if (StringUtils.hasText(simpleCron)) {
            this.scheduler = SimpleCron.compile(simpleCron).createScheduler();
        }
    }

    public void setScheduler(final Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @StroomShutdown
    public synchronized void stop() {
        finish = true;
        final StroomZipRepository proxyRepository = activeRepository.getAndSet(null);
        if (proxyRepository != null) {
            proxyRepository.finish();
        }
    }

    //setter for spring
    public void setZipFilenameDelimiter(final String zipFilenameDelimiter) {
        this.zipFilenameDelimiter = zipFilenameDelimiter;
    }
}
