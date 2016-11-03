package stroom.proxy.handler;

import stroom.proxy.feed.remote.GetFeedStatusRequest;
import stroom.proxy.feed.remote.GetFeedStatusResponse;

public interface LocalFeedService {
    GetFeedStatusResponse getFeedStatus(GetFeedStatusRequest request);
}
