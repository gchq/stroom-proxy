package stroom.proxy.util.spring;

import java.util.concurrent.atomic.AtomicBoolean;

import stroom.proxy.util.logging.StroomLogger;
import stroom.proxy.util.shared.Task;

public class StroomBeanMethodExecutable {
    private static final StroomLogger LOGGER = StroomLogger.getLogger(StroomBeanMethodExecutable.class);

    private final StroomBeanMethod stroomBeanMethod;
    private final StroomBeanStore stroomBeanStore;
    private final String message;
    private final AtomicBoolean running;

    public StroomBeanMethodExecutable(final StroomBeanMethod stroomBeanMethod, final StroomBeanStore stroomBeanStore,
                                      final String message) {
        this(stroomBeanMethod, stroomBeanStore, message, new AtomicBoolean());
    }

    public StroomBeanMethodExecutable(final StroomBeanMethod stroomBeanMethod, final StroomBeanStore stroomBeanStore,
                                      final String message, final AtomicBoolean running) {
        this.stroomBeanMethod = stroomBeanMethod;
        this.stroomBeanStore = stroomBeanStore;
        this.message = message;
        this.running = running;
    }

    public void exec(final Task<?> task) {
        try {
            LOGGER.info(message + " " + stroomBeanMethod.getBeanName() + "." + stroomBeanMethod.getBeanMethod().getName());
            final Class<?>[] paramTypes = stroomBeanMethod.getBeanMethod().getParameterTypes();
            if (paramTypes.length > 0) {
                if (paramTypes.length > 1) {
                    throw new IllegalArgumentException(
                            "Method cannot have more than 1 argument and that argument must be a task");
                } else {
                    final Class<?> firstParam = paramTypes[0];
                    if (!Task.class.isAssignableFrom(firstParam)) {
                        throw new IllegalArgumentException("Method can only have a task argument");
                    } else {
                        stroomBeanStore.invoke(stroomBeanMethod, task);
                    }
                }
            } else {
                stroomBeanStore.invoke(stroomBeanMethod);
            }
        } catch (final Throwable t) {
            LOGGER.error("Error calling %s", stroomBeanMethod, t);
        } finally {
            running.set(false);
        }
    }

    @Override
    public String toString() {
        return stroomBeanMethod.toString();
    }
}
