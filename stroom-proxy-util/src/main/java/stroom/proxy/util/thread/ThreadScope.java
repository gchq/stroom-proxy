package stroom.proxy.util.thread;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import stroom.proxy.util.logging.StroomLogger;

public class ThreadScope implements Scope {
    protected static final StroomLogger LOGGER = StroomLogger.getLogger(ThreadScope.class);

    @SuppressWarnings({ "rawtypes" })
    @Override
    public Object get(final String name, final ObjectFactory factory) {
        Object result = null;

        try {
            final ThreadScopeContext context = ThreadScopeContextHolder.getContext();
            if (context != null) {
                result = context.get(name);
                if (result == null) {
                    result = factory.getObject();
                    context.put(name, result);
                }
            }
        } catch (final RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }

        return result;
    }

    @Override
    public Object remove(final String name) {
        Object result = null;

        try {
            final ThreadScopeContext context = ThreadScopeContextHolder.getContext();
            if (context != null) {
                result = context.remove(name);
            }
        } catch (final RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }

        return result;
    }

    @Override
    public void registerDestructionCallback(final String name, final Runnable runnable) {
        try {
            final ThreadScopeContext context = ThreadScopeContextHolder.getContext();
            if (context != null) {
                context.registerDestructionCallback(name, runnable);
            }
        } catch (final RuntimeException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Object resolveContextualObject(final String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return null;
    }
}
