package stroom.proxy.util.thread;

/**
 * Class to control access to the thread scope context.
 */
public class ThreadScopeContextHolder {
    private static final ThreadLocal<ThreadScopeContext> THREAD_LOCAL = new ThreadLocal<ThreadScopeContext>();

    public static void setContext(final ThreadScopeContext context) {
        THREAD_LOCAL.set(context);
    }

    /**
     * Get the current context if there is one or throws an illegal state
     * exception. This should be used when a context is expected to already
     * exist.
     */
    public static ThreadScopeContext getContext() throws IllegalStateException {
        final ThreadScopeContext context = THREAD_LOCAL.get();
        if (context == null) {
            throw new IllegalStateException("No thread scope context active");
        }
        return context;
    }

    /**
     * Gets the current context if there is one or returns null if one isn't
     * currently in use.
     */
    static ThreadScopeContext currentContext() {
        return THREAD_LOCAL.get();
    }

    public static boolean contextExists() {
        return currentContext() != null;
    }

    /**
     * Called to create a thread scope context.
     */
    public static void createContext() {
        setContext(new ThreadScopeContext());
    }

    /**
     * Called to destroy the thread scope context.
     */
    public static void destroyContext() throws IllegalStateException {
        final ThreadScopeContext context = getContext();
        context.clear();
        setContext(null);
    }
}
