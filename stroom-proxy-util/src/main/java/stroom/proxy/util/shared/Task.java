package stroom.proxy.util.shared;

/**
 * Interface to all tasks.
 */
public interface Task<R> extends HasTerminate {
    TaskId getId();

    String getTaskName();

    String getSessionId();

    String getUserId();

    ThreadPool getThreadPool();
}
