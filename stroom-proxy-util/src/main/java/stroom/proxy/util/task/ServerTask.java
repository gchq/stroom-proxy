package stroom.proxy.util.task;

import stroom.proxy.util.shared.ModelStringUtil;
import stroom.proxy.util.shared.Monitor;
import stroom.proxy.util.shared.SimpleThreadPool;
import stroom.proxy.util.shared.Task;
import stroom.proxy.util.shared.TaskId;
import stroom.proxy.util.shared.ThreadPool;

public abstract class ServerTask<R> implements Task<R>, HasMonitor {
    private static final ThreadPool THREAD_POOL = new SimpleThreadPool(2);

    private final TaskId id;
    private final String sessionId;
    private final String userId;
    private final MonitorImpl monitor;
    private volatile String taskName;

    public ServerTask() {
        this.id = TaskIdFactory.create();
        this.sessionId = null;
        this.userId = null;
        this.monitor = new MonitorImpl();
    }

    public ServerTask(final Task<?> parentTask) {
        this(parentTask, parentTask.getSessionId(), parentTask.getUserId());
    }

    public ServerTask(final Task<?> parentTask, final String sessionId, final String userId) {
        if (parentTask == null) {
            this.id = TaskIdFactory.create();
        } else {
            this.id = TaskIdFactory.create(parentTask.getId());
        }
        this.sessionId = sessionId;
        this.userId = userId;

        // Rather than remember the parent task I just grab the parent monitor.
        // This avoids some Hessian serialisation issues that can occur
        // unexpectedly when sending parent tasks across the wire.
        if (parentTask != null && parentTask instanceof HasMonitor) {
            final HasMonitor hasMonitor = (HasMonitor) parentTask;
            this.monitor = new MonitorImpl(hasMonitor.getMonitor());
        } else {
            this.monitor = new MonitorImpl();
        }
    }

    @Override
    public TaskId getId() {
        return id;
    }

    @Override
    public String getTaskName() {
        if (taskName == null) {
            this.taskName = generateTaskName();
        }
        return taskName;
    }

    private String generateTaskName() {
        final String name = getClass().getSimpleName();
        ModelStringUtil.toDisplayValue(name);
        return name;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !(obj instanceof Task<?>)) {
            return false;
        }

        final Task<?> task = (Task<?>) obj;
        return id.equals(task.getId());
    }

    protected void setTaskName(final String newTaskName) {
        this.taskName = newTaskName;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return getTaskName() + " - " + id;
    }

    @Override
    public Monitor getMonitor() {
        return monitor;
    }

    /**
     * CONVENIENCE METHODS TO SHORTCUT REFERENCES TO MONITOR TO CHECK
     * TERMINATION ETC.
     **/
    @Override
    public boolean isTerminated() {
        return monitor.isTerminated();
    }

    @Override
    public void terminate() {
        monitor.terminate();
    }

    @Override
    public ThreadPool getThreadPool() {
        return THREAD_POOL;
    }
}
