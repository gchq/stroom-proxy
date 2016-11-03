package stroom.proxy.util.thread;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import stroom.proxy.util.logging.StroomLogger;

/**
 * Class to hold the spring thread bound variables.
 */
public class ThreadScopeContext {
    protected static final StroomLogger LOGGER = StroomLogger.getLogger(ThreadScopeContext.class);

    private final Map<String, Object> beanMap;

    private final Map<String, Runnable> requestDestructionCallback;

    public ThreadScopeContext() {
        beanMap = new HashMap<>();
        requestDestructionCallback = new LinkedHashMap<>(8);
    }

    final Object getMutex() {
        return beanMap;
    }

    final Object get(final String name) {
        return beanMap.get(name);
    }

    public final Object put(final String name, final Object bean) {
        return beanMap.put(name, bean);
    }

    final Object remove(final String name) {
        requestDestructionCallback.remove(name);
        return beanMap.remove(name);
    }

    final void registerDestructionCallback(final String name, final Runnable runnable) {
        requestDestructionCallback.put(name, runnable);
    }

    final void clear() {
        for (final String key : requestDestructionCallback.keySet()) {
            requestDestructionCallback.get(key).run();
        }

        requestDestructionCallback.clear();
        beanMap.clear();
    }
}
