package stroom.proxy.util.zip;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface StroomHeaderArguments {
    final static String GUID = "GUID";
    final static String COMPRESSION = "Compression";
    final static String COMPRESSION_ZIP = "ZIP";
    final static String COMPRESSION_GZIP = "GZIP";
    final static String COMPRESSION_NONE = "NONE";

    final static Set<String> VALID_COMPRESSION_SET = Collections
            .unmodifiableSet(new HashSet<String>(Arrays.asList(COMPRESSION_GZIP, COMPRESSION_ZIP, COMPRESSION_NONE)));

    final static String CONTENT_LENGTH = "content-length";
    final static String USER_AGENT = "user-agent";

    final static String REMOTE_ADDRESS = "RemoteAddress";
    final static String REMOTE_HOST = "RemoteHost";
    final static String RECEIVED_TIME = "ReceivedTime";
    final static String RECEIVED_PATH = "ReceivedPath";
    final static String EFFECTIVE_TIME = "EffectiveTime";
    final static String REMOTE_DN = "RemoteDN";
    final static String REMOTE_CERT_EXPIRY = "RemoteCertExpiry";
    final static String REMOTE_FILE = "RemoteFile";

    final static String STREAM_SIZE = "StreamSize";

    final static String STROOM_STATUS = "Stroom-Status";

    final static String FEED = "Feed";

    final static Set<String> HEADER_CLONE_EXCLUDE_SET = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList("accept", "connection", "content-length", "transfer-encoding", "expect", COMPRESSION)));

}
