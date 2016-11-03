package stroom.proxy.util.shared;

public class SimpleThreadPool implements ThreadPool {
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = Integer.MAX_VALUE;

    private final int priority;
    private final String name;

    public SimpleThreadPool(final int priority) {
        this("STROOM P" + priority, priority);
    }

    public SimpleThreadPool(final String name, final int priority) {
        this.name = name;
        this.priority = priority;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public int getCorePoolSize() {
        return CORE_POOL_SIZE;
    }

    @Override
    public int getMaxPoolSize() {
        return MAX_POOL_SIZE;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SimpleThreadPool)) {
            return false;
        }

        final SimpleThreadPool simpleThreadPool = (SimpleThreadPool) obj;
        return simpleThreadPool.name.equals(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
