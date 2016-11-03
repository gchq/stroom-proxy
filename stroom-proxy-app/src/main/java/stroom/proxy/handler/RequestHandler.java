package stroom.proxy.handler;

import java.io.IOException;

import stroom.proxy.util.zip.StroomStreamHandler;

public interface RequestHandler extends StroomStreamHandler {
    void validate();

    void handleHeader() throws IOException;

    void handleFooter() throws IOException;

    void handleError() throws IOException;

}
