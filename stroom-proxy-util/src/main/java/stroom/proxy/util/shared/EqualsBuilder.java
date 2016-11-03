package stroom.proxy.util.shared;

/**
 * A GWT friendly version of commons EqualsBuilder.
 */
public class EqualsBuilder {
    private boolean isEquals = true;

    public EqualsBuilder appendSuper(final boolean value) {
        if (!isEquals) {
            return this;
        }
        isEquals = value;
        return this;
    }

    public EqualsBuilder append(final Object lhs, final Object rhs) {
        if (!isEquals) {
            return this;
        }
        if (lhs == rhs) {
            return this;
        }
        if (lhs == null || rhs == null) {
            isEquals = false;
            return this;
        }
        isEquals = lhs.equals(rhs);
        return this;
    }

    public EqualsBuilder append(final long lhs, final long rhs) {
        if (!isEquals) {
            return this;
        }

        isEquals = (lhs == rhs);
        return this;
    }

    public EqualsBuilder append(final int lhs, final int rhs) {
        if (!isEquals) {
            return this;
        }

        isEquals = (lhs == rhs);
        return this;
    }

    public EqualsBuilder append(final short lhs, final short rhs) {
        if (!isEquals) {
            return this;
        }

        isEquals = (lhs == rhs);
        return this;
    }

    public EqualsBuilder append(final char lhs, final char rhs) {
        if (!isEquals) {
            return this;
        }

        isEquals = (lhs == rhs);
        return this;
    }

    public EqualsBuilder append(final byte lhs, final byte rhs) {
        if (!isEquals) {
            return this;
        }

        isEquals = (lhs == rhs);
        return this;
    }

    public EqualsBuilder append(final double lhs, final double rhs) {
        if (!isEquals) {
            return this;
        }

        isEquals = (lhs == rhs);
        return this;
    }

    public EqualsBuilder append(final float lhs, final float rhs) {
        if (!isEquals) {
            return this;
        }

        isEquals = (lhs == rhs);
        return this;
    }

    public EqualsBuilder append(final boolean lhs, final boolean rhs) {
        if (!isEquals) {
            return this;
        }

        isEquals = (lhs == rhs);
        return this;
    }

    public boolean isEquals() {
        return isEquals;
    }
}
