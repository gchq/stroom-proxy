package stroom.proxy.util.scheduler;

import stroom.proxy.util.date.DateUtil;
import stroom.proxy.util.shared.ModelStringUtil;

public class FrequencyScheduler implements Scheduler {
    private final long interval;
    private volatile long intervalToUse;
    private volatile long lastExecution;

    public FrequencyScheduler(final String frequency) {
        if (frequency == null || frequency.trim().length() == 0) {
            throw new NumberFormatException("Frequency expression cannot be null");
        }

        final Long duration = ModelStringUtil.parseDurationString(frequency);
        if (duration == null) {
            throw new NumberFormatException("Unable to parse frequency expression");
        }

        interval = duration;
        calculateIntervalToUse();
        lastExecution = System.currentTimeMillis();
    }

    /**
     * Add in a +-5% random on the interval so that all jobs don't fire at same
     * time.
     */
    private void calculateIntervalToUse() {
        intervalToUse = interval;
        if (intervalToUse > 100) {
            double offset = intervalToUse;
            offset = offset * 0.05D * (Math.random() - 0.5D);
            intervalToUse += offset;
        }
    }

    @Override
    public boolean execute() {
        final long now = System.currentTimeMillis();
        if (lastExecution + intervalToUse <= now) {
            lastExecution = now;
            calculateIntervalToUse();
            return true;
        }

        return false;
    }

    @Override
    public Long getScheduleReferenceTime() {
        return lastExecution;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("FrequencyScheduler ");
        sb.append("lastExecution=\"");
        sb.append(DateUtil.createNormalDateTimeString(lastExecution));
        sb.append("\" ");
        sb.append("interval=\"");
        sb.append(ModelStringUtil.formatDurationString(interval));
        sb.append("\" ");
        return sb.toString();
    }
}
