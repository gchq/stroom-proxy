package stroom.proxy.util.io;

public class ByteSlice {
    private final byte[] array;
    private final int off;
    private final int len;

    @SuppressWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
    public ByteSlice(final byte[] array, final int off, final int len) {
        this.array = array;
        this.off = off;
        this.len = len;
    }

    @SuppressWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
    public ByteSlice(final byte[] array) {
        this.array = array;
        this.off = 0;
        this.len = array.length;
    }

    @SuppressWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
    public byte[] getArray() {
        return array;
    }

    public int getOff() {
        return off;
    }

    public int getLen() {
        return len;
    }
}
