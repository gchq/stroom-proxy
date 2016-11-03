package stroom.proxy.handler.remoteclient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Resource;

import com.googlecode.ehcache.annotations.Cacheable;
import com.googlecode.ehcache.annotations.KeyGenerator;

import stroom.proxy.feed.remote.GetFeedStatusRequest;
import stroom.proxy.feed.remote.GetFeedStatusResponse;
import stroom.proxy.feed.remote.RemoteFeedService;
import stroom.proxy.handler.LocalFeedService;
import stroom.proxy.util.logging.StroomLogger;

public class RemoteFeedServiceCacheable implements LocalFeedService {
    private static StroomLogger LOGGER = StroomLogger.getLogger(RemoteFeedServiceCacheable.class);

    @Resource
    private RemoteFeedService remoteFeedService;

    private Map<GetFeedStatusRequest, GetFeedStatusResponse> lastKnownRepsonse = new ConcurrentHashMap<GetFeedStatusRequest, GetFeedStatusResponse>();

    @Cacheable(cacheName = "remoteCache", keyGenerator = @KeyGenerator(name = "ListCacheKeyGenerator") )
    public GetFeedStatusResponse getFeedStatus(GetFeedStatusRequest request) {
        GetFeedStatusResponse response = null;
        try {
            response = remoteFeedService.getFeedStatus(request);
            lastKnownRepsonse.put(request, response);
        } catch (Exception ex) {
            LOGGER.debug("handleHeader() - Unable to check remote feed service", ex);
            response = lastKnownRepsonse.get(request);
            if (response != null) {
                LOGGER.error(
                        "handleHeader() - Unable to check remote feed service (%s).... will use last response (%s) - %s",
                        request, response, ex.getMessage());
            } else {
                response = new GetFeedStatusResponse();
                LOGGER.error("handleHeader() - Unable to check remote feed service (%s).... will assume OK (%s) - %s",
                        request, response, ex.getMessage());
            }
        }

        LOGGER.debug("getFeedStatus() " + request + " -> " + response);
        return response;
    }
}
