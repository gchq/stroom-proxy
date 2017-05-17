package stroom.proxy.handler;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public interface StroomHeaderArguments {
    String GUID = "GUID";
    String COMPRESSION = "Compression";
    String COMPRESSION_ZIP = "ZIP";
    String COMPRESSION_GZIP = "GZIP";
    String COMPRESSION_NONE = "NONE";

    Set<String> VALID_COMPRESSION_SET = Collections
            .unmodifiableSet(new HashSet<>(Arrays.asList(COMPRESSION_GZIP, COMPRESSION_ZIP, COMPRESSION_NONE)));

    String CONTENT_LENGTH = "content-length";

    String REMOTE_ADDRESS = "RemoteAddress";
    String REMOTE_HOST = "RemoteHost";
    String RECEIVED_TIME = "ReceivedTime";
    String RECEIVED_PATH = "ReceivedPath";
    String REMOTE_DN = "RemoteDN";
    String REMOTE_CERT_EXPIRY = "RemoteCertExpiry";

    String STREAM_SIZE = "StreamSize";

    String STROOM_STATUS = "Stroom-Status";

    String FEED = "Feed";

    Set<String> HEADER_CLONE_EXCLUDE_SET = Collections.unmodifiableSet(new HashSet<>(
            Arrays.asList("accept", "connection", "content-length", "transfer-encoding", "expect", COMPRESSION)));
}