package stroom.proxy.util.concurrent;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Added sequencer that handles wrapping so AtomicSequence(3) would give you
 * back 0,1,2,0,1,2 etc. or you can a variable limit as = new AtomicSequence();
 * as.next(3); as.next(3);
 */
public class AtomicSequence {
    private final AtomicLong sequence = new AtomicLong();
    private final int limit;

    public AtomicSequence() {
        this(Integer.MAX_VALUE);
    }

    public AtomicSequence(final int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than zero ");
        }
        this.limit = limit;
    }

    /**
     * @return the next sequence within the default limit
     */
    public int next() {
        return next(limit);
    }

    /**
     * @return return a sequence within the limit
     */
    public int next(final int limit) {
        for (;;) {
            long current = sequence.get();

            if (current > limit) {
                long next = current - limit + 1;
                if (sequence.compareAndSet(current, next)) {
                    return (int) current % limit;
                }
            } else {
                long next = current + 1;
                if (sequence.compareAndSet(current, next)) {
                    return (int) current % limit;
                }
            }
        }
    }

    /**
     * Reset the sequence
     */
    public void reset() {
        sequence.set(0);
    }
}
