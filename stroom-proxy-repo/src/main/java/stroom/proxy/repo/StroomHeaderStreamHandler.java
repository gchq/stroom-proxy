package stroom.proxy.repo;

import java.io.IOException;

public interface StroomHeaderStreamHandler {
    void handleHeader(HeaderMap headerMap) throws IOException;

}
