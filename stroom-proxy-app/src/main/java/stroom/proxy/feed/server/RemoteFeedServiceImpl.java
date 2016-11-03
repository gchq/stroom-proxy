package stroom.proxy.feed.server;

import javax.annotation.Resource;

import stroom.proxy.feed.remote.GetFeedStatusRequest;
import stroom.proxy.feed.remote.GetFeedStatusResponse;
import stroom.proxy.feed.remote.RemoteFeedService;
import stroom.proxy.datafeed.ProxyHandlerFactory;
import stroom.proxy.util.logging.StroomLogger;
import stroom.proxy.util.logging.LogExecutionTime;

public class RemoteFeedServiceImpl implements RemoteFeedService {
    private static StroomLogger LOGGER = StroomLogger.getLogger(RemoteFeedServiceImpl.class);

    @Resource
    ProxyHandlerFactory proxyHandlerFactory;

    @Override
    public GetFeedStatusResponse getFeedStatus(GetFeedStatusRequest request) {
        LogExecutionTime logExecutionTime = new LogExecutionTime();
        GetFeedStatusResponse response = proxyHandlerFactory.getLocalFeedService().getFeedStatus(request);
        LOGGER.debug("getFeedStatus() - %s -> %s in %s", request, response, logExecutionTime);
        return response;
    }

}
