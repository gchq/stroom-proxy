package stroom.proxy.util.spring;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import stroom.proxy.util.logging.StroomLogger;

@Component
public class StroomBeanLifeCycle {
    private final StroomLogger LOGGER = StroomLogger.getLogger(StroomBeanLifeCycle.class);

    @Resource
    private StroomBeanStore stroomBeanStore;

    private volatile boolean shuttingDown;
    private volatile boolean initialised = false;

    private volatile List<StroomBeanMethod> startPendingBeans = null;
    private volatile List<StroomBeanMethod> stopPendingBeans = null;

    /**
     * @return things that need running at start up of null if they have all
     *         started
     */
    public StroomBeanMethodExecutable getStartExecutable() {
        init();
        // Keep looping until we have returned everything
        while (true) {
            // Don't try and start anymore
            if (shuttingDown) {
                return null;
            }

            // everything running !
            if (startPendingBeans.size() == 0) {
                return null;
            }

            for (final StroomBeanMethod stroomBeanMethod : startPendingBeans) {
                addBeanStarting(stroomBeanMethod);
                return new StroomBeanMethodExecutable(stroomBeanMethod, stroomBeanStore, "Starting");
            }
        }
    }

    /**
     * @return things that need running at start up of null if they have all
     *         started
     */
    public StroomBeanMethodExecutable getStopExecutable() {
        init();
        // Keep looping until we have returned everything
        while (true) {
            // everything running !
            if (stopPendingBeans.size() == 0) {
                return null;
            }

            for (final StroomBeanMethod stroomBeanMethod : stopPendingBeans) {
                addBeanStopping(stroomBeanMethod);
                return new StroomBeanMethodExecutable(stroomBeanMethod, stroomBeanStore, "Stopping");
            }
        }
    }

    private synchronized void addBeanStarting(final StroomBeanMethod stroomBeanMethod) {
        startPendingBeans.remove(stroomBeanMethod);
    }

    private synchronized void addBeanStopping(final StroomBeanMethod stroomBeanMethod) {
        stopPendingBeans.remove(stroomBeanMethod);
    }

    private synchronized void init() {
        if (initialised) {
            return;
        }

        startPendingBeans = new ArrayList<StroomBeanMethod>(stroomBeanStore.getStroomBeanMethod(StroomStartup.class));
        Collections.sort(startPendingBeans, new Comparator<StroomBeanMethod>() {
            @Override
            public int compare(final StroomBeanMethod o1, final StroomBeanMethod o2) {
                final StroomStartup stroomStartup1 = o1.getBeanMethod().getAnnotation(StroomStartup.class);
                final StroomStartup stroomStartup2 = o2.getBeanMethod().getAnnotation(StroomStartup.class);

                int compare = Integer.compare(stroomStartup2.priority(), stroomStartup1.priority());
                if (compare != 0) {
                    // We want to reverse sort so that highest priority
                    // startup methods come first.
                    return compare;
                }

                compare = o1.getBeanName().compareTo(o2.getBeanName());
                if (compare != 0) {
                    return compare;
                }

                return o1.getBeanMethod().getName().compareTo(o2.getBeanMethod().getName());
            }
        });

        stopPendingBeans = new ArrayList<StroomBeanMethod>(stroomBeanStore.getStroomBeanMethod(StroomShutdown.class));
        Collections.sort(stopPendingBeans, new Comparator<StroomBeanMethod>() {
            @Override
            public int compare(final StroomBeanMethod o1, final StroomBeanMethod o2) {
                final StroomShutdown stroomShutdown1 = o1.getBeanMethod().getAnnotation(StroomShutdown.class);
                final StroomShutdown stroomShutdown2 = o2.getBeanMethod().getAnnotation(StroomShutdown.class);

                int compare = Integer.compare(stroomShutdown2.priority(), stroomShutdown1.priority());
                if (compare != 0) {
                    // We want to reverse sort so that highest priority
                    // shutdown methods come first.
                    return compare;
                }

                compare = o1.getBeanName().compareTo(o2.getBeanName());
                if (compare != 0) {
                    return compare;
                }

                return o1.getBeanMethod().getName().compareTo(o2.getBeanMethod().getName());
            }
        });

        initialised = true;
    }
}
