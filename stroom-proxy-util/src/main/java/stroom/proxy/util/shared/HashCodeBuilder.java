package stroom.proxy.util.shared;

/**
 * A GWT friendly version of commons HashCodeBuilder.
 */
public class HashCodeBuilder {
    private int hashCode = 31;

    public void appendSuper(final int value) {
        hashCode += value;
    }

    public void append(final Object o) {
        if (o == null) {
            hashCode = hashCode * 31;
        } else {
            hashCode = hashCode * 31 + o.hashCode();
        }
    }

    public void append(final long value) {
        hashCode = (int) (((long) hashCode) * 31L + value);
    }

    public void append(final int value) {
        hashCode = hashCode * 31 + value;
    }

    public void append(final short value) {
        hashCode = hashCode * 31 + value;
    }

    public void append(final char value) {
        hashCode = hashCode * 31 + value;
    }

    public void append(final byte value) {
        hashCode = hashCode * 31 + value;
    }

    public void append(final double value) {
        hashCode = (int) (hashCode * 31 + value);
    }

    public void append(final float value) {
        hashCode = (int) (hashCode * 31 + value);
    }

    public void append(final boolean value) {
        hashCode = hashCode * 31 + (value ? 0 : 1);
    }

    public int toHashCode() {
        return hashCode;
    }
}
