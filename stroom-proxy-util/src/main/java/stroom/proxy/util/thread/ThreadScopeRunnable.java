package stroom.proxy.util.thread;

public abstract class ThreadScopeRunnable implements Runnable {
    @Override
    public final void run() {
        final ThreadScopeContext orginalContext = ThreadScopeContextHolder.currentContext();
        ThreadScopeContextHolder.createContext();
        try {
            exec();
        } finally {
            ThreadScopeContextHolder.destroyContext();
            if (orginalContext != null) {
                ThreadScopeContextHolder.setContext(orginalContext);
            }
        }
    }

    protected abstract void exec();
}
