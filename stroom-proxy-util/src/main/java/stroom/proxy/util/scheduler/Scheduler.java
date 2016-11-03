package stroom.proxy.util.scheduler;

public interface Scheduler {
    /**
     * Should we execute.
     *
     * @return True if it is time to execute.
     */
    boolean execute();

    Long getScheduleReferenceTime();
}
