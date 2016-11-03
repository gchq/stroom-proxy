package stroom.proxy.util.task;

import stroom.proxy.util.logging.StroomLogger;
import stroom.proxy.util.shared.Task;

/**
 * Class to control access to the thread scope context.
 */
public class TaskScopeContextHolder {
    private static StroomLogger LOGGER = StroomLogger.getLogger(TaskScopeContextHolder.class);

    private static final ThreadLocal<TaskScopeContext> THREAD_LOCAL_CONTEXT = new InheritableThreadLocal<TaskScopeContext>();

    private static void setContext(final TaskScopeContext context) {
        THREAD_LOCAL_CONTEXT.set(context);
    }

    /**
     * Get the current context if there is one or throws an illegal state
     * exception. This should be used when a context is expected to already
     * exist.
     */
    public static TaskScopeContext getContext() throws IllegalStateException {
        final TaskScopeContext context = THREAD_LOCAL_CONTEXT.get();
        if (context == null) {
            throw new IllegalStateException("No task scope context active");
        }
        return context;
    }

    /**
     * Gets the current context if there is one or returns null if one isn't
     * currently in use.
     */
    private static TaskScopeContext currentContext() {
        return THREAD_LOCAL_CONTEXT.get();
    }

    public static boolean contextExists() {
        return currentContext() != null;
    }

    /**
     * Called to add a task scope context.
     */
    public static void addContext() {
        final TaskScopeContext context = currentContext();
        if (context != null) {
            addContext(context.getTask());
        } else {
            addContext(null);
        }
    }

    /**
     * Called to add a task scope context.
     */
    public static void addContext(final Task<?> task) {
        final TaskScopeContext context = currentContext();
        final TaskScopeContext taskScopeContext = new TaskScopeContext(context, task);
        setContext(taskScopeContext);
    }

    /**
     * Called to remove the task scope context.
     */
    public static void removeContext() throws IllegalStateException {
        try {
            final TaskScopeContext context = getContext();
            // Switch the context to the parent context.
            final TaskScopeContext parentContext = context.getParent();
            setContext(parentContext);

            // Destroy previous context.
            context.clear();
        } catch (final RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }
}
