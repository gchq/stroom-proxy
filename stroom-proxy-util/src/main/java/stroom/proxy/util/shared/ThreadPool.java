package stroom.proxy.util.shared;

public interface ThreadPool {
    String getName();

    int getPriority();

    int getCorePoolSize();

    int getMaxPoolSize();
}
