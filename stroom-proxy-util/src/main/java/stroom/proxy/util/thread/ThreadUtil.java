package stroom.proxy.util.thread;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import stroom.proxy.util.logging.StroomLogger;

public final class ThreadUtil {
//    private static final StroomLogger LOGGER = StroomLogger.getLogger(ThreadUtil.class);

    private ThreadUtil() {
        // Utility class so hide constructor.
    }

//    public final static boolean sleepTenSeconds() {
//        if (LOGGER.isDebugEnabled()) {
//            LOGGER.debug("sleepTenSeconds() - Sleeping");
//        }
//
//        final boolean success = sleep(10000);
//
//        if (LOGGER.isDebugEnabled()) {
//            LOGGER.debug("sleepTenSeconds() - Done");
//        }
//
//        return success;
//    }

    public final static boolean sleep(final long millis) {
        try {
            if (millis > 0) {
                Thread.sleep(millis);
            }
        } catch (final InterruptedException e) {
            // It's not an error that we were Interrupted!! Don't log the
            // exception !
            return false;
        }

        return true;
    }

    public final static boolean sleep(final Long millis) {
        if (millis != null) {
            return sleep(millis.longValue());
        }
        return true;
    }

//    /**
//     * Method to add a random delay or if none to yield to give any waiting
//     * threads a chance. This helps make test threaded code more random to track
//     * synchronized issues.
//     *
//     * @param millis
//     *            up to the many ms
//     * @return actual sleep time
//     */
//    public final static int sleepUpTo(final long millis) {
//        try {
//            if (millis > 0) {
//                int realSleep = (int) Math.floor(Math.random() * (millis + 1));
//                if (realSleep > 0) {
//                    Thread.sleep(realSleep);
//                    return realSleep;
//                } else {
//                    // Give any waiting threads a go
//                    Thread.yield();
//                    return 0;
//                }
//            }
//        } catch (final InterruptedException e) {
//            // It's not an error that we were Interrupted!! Don't log the
//            // exception !
//            return -1;
//        }
//        return 0;
//    }
//
//    /**
//     * Try repeatedly to acquire a lock but give up if the supplied task is
//     * terminated.
//     *
//     * @param lock
//     *            The lock to acquire.
//     * @param task
//     *            The task to check for termination.
//     * @return True if the lock is acquired successfully.
//     */
//    public final static boolean acquireLock(final ReentrantLock lock, final ServerTask<?> task) {
//        if (task == null) {
//            throw new NullPointerException("Null task");
//        }
//
//        boolean acquiredLock = false;
//        try {
//            while (!acquiredLock && !task.isTerminated()) {
//                acquiredLock = lock.tryLock(1, TimeUnit.SECONDS);
//            }
//        } catch (final InterruptedException e) {
//            LOGGER.error(e.getMessage(), e);
//        }
//        return acquiredLock;
//    }
}
