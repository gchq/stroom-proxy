package stroom.proxy.util.task;

import java.util.UUID;

import stroom.proxy.util.shared.TaskId;
import stroom.proxy.util.shared.TaskIdImpl;

public class TaskIdFactory {
    public static TaskId create() {
        return new TaskIdImpl(createUUID(), null);
    }

    public static TaskId create(final TaskId parentTaskId) {
        if (parentTaskId != null) {
            return new TaskIdImpl(createUUID(), parentTaskId);
        }

        return create();
    }

    private static String createUUID() {
        return UUID.randomUUID().toString();
    }
}
