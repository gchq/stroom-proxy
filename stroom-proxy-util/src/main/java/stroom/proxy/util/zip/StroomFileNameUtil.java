package stroom.proxy.util.zip;

import stroom.proxy.repo.PathCreator;
import stroom.proxy.util.logging.StroomLogger;

import java.util.Arrays;

/**
 * Utility to build a path for a given id in such a way that we don't exceed OS
 * dir file limits.
 * <p>
 * So file 1 is 001 and file 1000 is 001/000 etc...
 */
public class StroomFileNameUtil {

    public final static int MAX_FILENAME_LENGTH = 255;
    private static final StroomLogger LOGGER = StroomLogger.getLogger(StroomFileNameUtil.class);

    private StroomFileNameUtil() {
        //static util methods only
    }

    public static String getDirPathForId(long id) {
        return buildPath(id, true);
    }

    /**
     * Build a file path for a id.
     */
    public static String getFilePathForId(long id) {
        return buildPath(id, false);
    }

    private static String buildPath(long id, boolean justDir) {
        StringBuilder fileBuffer = new StringBuilder();
        fileBuffer.append(id);
        // Pad out e.g. 10100 -> 010100
        while ((fileBuffer.length() % 3) != 0) {
            fileBuffer.insert(0, '0');
        }
        StringBuilder dirBuffer = new StringBuilder();
        for (int i = 0; i < fileBuffer.length() - 3; i += 3) {
            dirBuffer.append(fileBuffer.subSequence(i, i + 3));
            dirBuffer.append("/");
        }

        if (justDir) {
            return dirBuffer.toString();
        } else {
            return dirBuffer.toString() + fileBuffer.toString();
        }
    }

    public static String constructFilename(final String delimiter, long id, String... fileExtensions) {
        return constructFilename(delimiter, id, null, null, fileExtensions);

    }

    public static String constructFilename(final String delimiter, long id, final String filenameTemplate,
                                           final HeaderMap headerMap,
                                           String... fileExtensions) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Using delimiter [%s], filenameTemplate [%s] and fileExtensions [%s]",
                        delimiter,
                        filenameTemplate,
                        Arrays.toString(fileExtensions));
        }

        final StringBuilder filenameBuilder = new StringBuilder();
        String idStr = getFilePathForId(id);
        filenameBuilder.append(idStr);

        int extensionsLength = 0;
        StringBuilder extensions = new StringBuilder();
        if (fileExtensions != null) {
            for (String extension : fileExtensions) {
                if (extension != null) {
                    extensions.append(extension);
                    extensionsLength += extension.length();
                }
            }
        }

        if (filenameTemplate != null && !filenameTemplate.isEmpty()) {
            String zipFilenameDelimiter = delimiter == null ? "" : delimiter;
            int lengthAvailableForTemplatedPart = MAX_FILENAME_LENGTH - idStr.length() - extensionsLength;
            filenameBuilder.append(zipFilenameDelimiter);
            String expandedTemplate = PathCreator.replace(filenameTemplate, headerMap, lengthAvailableForTemplatedPart);
            filenameBuilder.append(expandedTemplate);
        }

        filenameBuilder.append(extensions.toString());
        String filename = filenameBuilder.toString();
        return filename;
    }

}
