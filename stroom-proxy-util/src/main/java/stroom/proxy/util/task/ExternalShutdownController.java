package stroom.proxy.util.task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import stroom.proxy.util.shared.TerminateHandler;

public class ExternalShutdownController {
    private static final Map<Object, TerminateHandler> terminateHandlers = new ConcurrentHashMap<Object, TerminateHandler>();

    public static void addTerminateHandler(final Object key, final TerminateHandler terminateHandler) {
        terminateHandlers.put(key, terminateHandler);
    }

    public static void shutdown() {
        terminateHandlers.values().forEach(TerminateHandler::onTerminate);
    }
}
