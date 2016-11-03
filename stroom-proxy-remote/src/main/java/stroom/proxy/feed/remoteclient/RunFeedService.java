package stroom.proxy.feed.remoteclient;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import stroom.proxy.feed.remote.GetFeedStatusRequest;
import stroom.proxy.feed.remote.RemoteFeedService;

public class RunFeedService {
    public static void main(String[] args) {
        try {
            ApplicationContext appContext = new ClassPathXmlApplicationContext(
                    new String[] { "classpath:META-INF/spring/stroomRemoteContext.xml",
                            "classpath:META-INF/spring/stroomRemoteClientContext.xml", });

            RemoteFeedService feedService = appContext.getBean(RemoteFeedService.class);

            feedService.getFeedStatus(new GetFeedStatusRequest());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
