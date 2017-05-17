
package stroom.proxy.handler.db;

import stroom.proxy.StroomStatusCode;
import stroom.proxy.feed.remote.FeedStatus;
import stroom.proxy.feed.remote.GetFeedStatusRequest;
import stroom.proxy.feed.remote.GetFeedStatusResponse;
import stroom.proxy.handler.DropStreamException;
import stroom.proxy.handler.LocalFeedService;
import stroom.proxy.handler.RequestHandler;
import stroom.proxy.handler.StroomHeaderArguments;
import stroom.proxy.handler.StroomStreamException;
import stroom.proxy.repo.HeaderMap;
import stroom.proxy.repo.StroomZipEntry;
import stroom.proxy.util.logging.StroomLogger;

import javax.annotation.Resource;
import java.io.IOException;

public class DataBaseValidatorRequestHandler implements RequestHandler {
    private final static StroomLogger LOGGER = StroomLogger.getLogger(DataBaseValidatorRequestHandler.class);

    @Resource
    HeaderMap headerMap;

    @Resource
    LocalFeedService localFeedService;

    @Override
    public void handleHeader() throws IOException {
        String feedName = headerMap.get(StroomHeaderArguments.FEED);
        if (feedName == null) {
            throw new StroomStreamException(StroomStatusCode.FEED_MUST_BE_SPECIFIED);
        }

        String senderDn = headerMap.get(StroomHeaderArguments.REMOTE_DN);
        GetFeedStatusRequest request = new GetFeedStatusRequest(feedName, senderDn);
        GetFeedStatusResponse response = localFeedService.getFeedStatus(request);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("handleHeader() - " + request + " -> " + response);
        }

        if (response.getStatus() == FeedStatus.Reject) {
            throw new StroomStreamException(StroomStatusCode.FEED_IS_NOT_SET_TO_RECEIVED_DATA);
        }

        if (response.getStatus() == FeedStatus.Drop) {
            throw new DropStreamException();
        }
    }

    @Override
    public void handleEntryStart(StroomZipEntry stroomZipEntry) throws IOException {
        // NA for DB Validator
    }

    @Override
    public void handleEntryEnd() throws IOException {
        // NA for DB Validator
    }

    @Override
    public void handleEntryData(byte[] buffer, int off, int len) throws IOException {
        // NA for DB Validator
    }

    @Override
    public void handleError() throws IOException {
        // NA for DB Validator
    }

    @Override
    public void handleFooter() throws IOException {
        // NA for DB Validator
    }

    @Override
    public void validate() {
    }

}
