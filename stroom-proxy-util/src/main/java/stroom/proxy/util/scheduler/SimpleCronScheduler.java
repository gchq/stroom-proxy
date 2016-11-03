package stroom.proxy.util.scheduler;

import stroom.proxy.util.date.DateUtil;

public class SimpleCronScheduler implements Scheduler {
    private final SimpleCron simpleCron;
    private Long lastExecute;
    private Long nextExecute;

    public SimpleCronScheduler(final String expression) {
        this.simpleCron = SimpleCron.compile(expression);
    }

    SimpleCronScheduler(final SimpleCron simpleCron) {
        this.simpleCron = simpleCron;
    }

    /**
     * @return date to aid testing.
     */
    protected Long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * Should we execute.
     *
     * @return
     */
    public boolean execute(final long timeNow) {
        final Long now = timeNow;
        if (nextExecute == null) {
            nextExecute = simpleCron.getNextTime(now);
        } else if (now > nextExecute) {
            nextExecute = simpleCron.getNextTime(now);
            lastExecute = now;
            return true;
        }
        return false;
    }

    /**
     * Should we execute.
     *
     * @return
     */
    @Override
    public boolean execute() {
        return execute(getCurrentTime());
    }

    @Override
    public Long getScheduleReferenceTime() {
        if (lastExecute != null) {
            return lastExecute;
        }

        return System.currentTimeMillis();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("SimpleCron ");
        if (lastExecute != null) {
            sb.append("lastExecute=\"");
            sb.append(DateUtil.createNormalDateTimeString(lastExecute));
            sb.append("\" ");
        }
        if (nextExecute != null) {
            sb.append("nextExecute=\"");
            sb.append(DateUtil.createNormalDateTimeString(nextExecute));
            sb.append("\" ");
        }
        return sb.toString();
    }
}
