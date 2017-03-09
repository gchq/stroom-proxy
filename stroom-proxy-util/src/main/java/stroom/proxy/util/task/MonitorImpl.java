package stroom.proxy.util.task;

import stroom.proxy.util.logging.LoggerUtil;
import stroom.proxy.util.shared.Monitor;
import stroom.proxy.util.shared.TerminateHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class MonitorImpl implements Monitor {
    private static final long serialVersionUID = 6158410874438193810L;

    private final Monitor parent;

    private volatile boolean terminate;

    @SuppressWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient volatile List<TerminateHandler> terminateHandlers;

    private final ReentrantLock terminateHandlersLock = new ReentrantLock();

    @SuppressWarnings(value = "VO_VOLATILE_REFERENCE_TO_ARRAY")
    private volatile Object[] info;

    public MonitorImpl() {
        this.parent = null;
    }

    public MonitorImpl(final Monitor parent) {
        this.parent = parent;
    }

    @Override
    public boolean isTerminated() {
        if (parent != null && parent.isTerminated()) {
            return true;
        }

        return terminate;
    }

    @Override
    public void terminate() {
        terminateHandlersLock.lock();
        try {
            this.terminate = true;
        } finally {
            terminateHandlersLock.unlock();
        }

        fireTerminateEvent();
    }

    @Override
    public void addTerminateHandler(final TerminateHandler handler) {
        terminateHandlersLock.lock();
        try {
            if (terminateHandlers == null) {
                terminateHandlers = new ArrayList<TerminateHandler>();
            }

            terminateHandlers.add(handler);

            // If we have already terminated then let the handler know.
            if (isTerminated()) {
                handler.onTerminate();
            }
        } finally {
            terminateHandlersLock.unlock();
        }
    }

    private void fireTerminateEvent() {
        terminateHandlersLock.lock();
        try {
            if (terminateHandlers != null) {
                for (final TerminateHandler terminateHandler : terminateHandlers) {
                    terminateHandler.onTerminate();
                }
            }
        } finally {
            terminateHandlersLock.unlock();
        }
    }

    @Override
    public String getInfo() {
        return LoggerUtil.buildMessage(info);
    }

    @Override
    public void info(final Object... args) {
        this.info = args;
    }

    @Override
    public String toString() {
        return getInfo();
    }

    @Override
    public Monitor getParent() {
        return parent;
    }
}
