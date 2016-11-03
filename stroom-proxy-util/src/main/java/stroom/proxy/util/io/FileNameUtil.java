package stroom.proxy.util.io;

public class FileNameUtil {
    public static String getBaseName(final String file) {
        final int i = file.lastIndexOf('.');
        if (i > 0) {
            return file.substring(0, i);
        }
        return file;
    }

    public static String getExtension(final String file) {
        final int i = file.lastIndexOf('.');
        if (i > 0) {
            return file.substring(i + 1);
        }
        return "";
    }
}
