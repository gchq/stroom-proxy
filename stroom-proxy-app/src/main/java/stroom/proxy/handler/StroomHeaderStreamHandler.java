package stroom.proxy.handler;

import stroom.proxy.repo.HeaderMap;

import java.io.IOException;

public interface StroomHeaderStreamHandler {
    void handleHeader(HeaderMap headerMap) throws IOException;

}
