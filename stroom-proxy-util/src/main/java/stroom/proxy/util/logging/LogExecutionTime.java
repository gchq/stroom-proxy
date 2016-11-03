package stroom.proxy.util.logging;

import stroom.proxy.util.shared.ModelStringUtil;

/**
 * Class to output timings.
 */
public class LogExecutionTime {
    private long startTime = System.currentTimeMillis();

    public long getDuration() {
        return System.currentTimeMillis() - startTime;
    }

    public long getStartTime() {
        return startTime;
    }

    @Override
    public String toString() {
        return ModelStringUtil.formatDurationString(getDuration());
    }
}
