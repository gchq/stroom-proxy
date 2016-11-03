package stroom.proxy.util.zip;

import java.io.IOException;

public interface StroomHeaderStreamHandler {
    void handleHeader(HeaderMap headerMap) throws IOException;

}
