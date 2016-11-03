package stroom.proxy.util.task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import stroom.proxy.util.shared.Task;

/**
 * Class to hold the spring task bound variables.
 */
public class TaskScopeContext {
    private final Map<String, Object> beanMap;

    private final Map<String, Runnable> requestDestructionCallback;

    private final TaskScopeContext parent;
    private final Task<?> task;

    public TaskScopeContext(final TaskScopeContext parent, final Task<?> task) {
        this.parent = parent;
        this.task = task;
        this.beanMap = new ConcurrentHashMap<>();
        this.requestDestructionCallback = new ConcurrentHashMap<>();
    }

    public Task<?> getTask() {
        return task;
    }

    final Object getMutex() {
        return beanMap;
    }

    final Object get(final String name) {
        return beanMap.get(name);
    }

    final Object put(final String name, final Object bean) {
        return beanMap.put(name, bean);
    }

    final Object remove(final String name) {
        requestDestructionCallback.remove(name);
        return beanMap.remove(name);
    }

    final void registerDestructionCallback(final String name, final Runnable runnable) {
        requestDestructionCallback.put(name, runnable);
    }

    public TaskScopeContext getParent() {
        return parent;
    }

    final void clear() {
        for (final String key : requestDestructionCallback.keySet()) {
            requestDestructionCallback.get(key).run();
        }

        requestDestructionCallback.clear();
        beanMap.clear();
    }
}
