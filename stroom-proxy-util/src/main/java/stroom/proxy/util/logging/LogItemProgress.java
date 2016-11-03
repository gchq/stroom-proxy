package stroom.proxy.util.logging;

import java.io.Serializable;

/**
 * Utility used in the logging code to output human readable progress for zero
 * based index processing. e.g. size = 10, index = 0, outputs "1/10"
 */
public class LogItemProgress implements Serializable {
    private static final long serialVersionUID = -8931028520798738334L;

    private long pos = 0;
    private long size = 0;

    public LogItemProgress(final long pos, final long size) {
        this.pos = pos;
        this.size = size;
    }

    public void incrementProgress() {
        pos++;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(pos);
        builder.append("/");
        builder.append(size);
        return builder.toString();
    }

}
