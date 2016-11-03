package stroom.proxy.status.remoteclient;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import stroom.proxy.status.remote.GetStatusRequest;
import stroom.proxy.status.remote.GetStatusResponse;
import stroom.proxy.status.remote.GetStatusResponse.StatusEntry;
import stroom.proxy.status.remote.RemoteStatusService;
import stroom.proxy.util.logging.StroomLogger;

public class RunStatusService {
    static StroomLogger log = StroomLogger.getLogger(RunStatusService.class);

    public static void main(String[] args) {
        try {
            ApplicationContext appContext = new ClassPathXmlApplicationContext(
                    new String[] { "classpath:META-INF/spring/stroomRemoteClientContext.xml" });

            RemoteStatusService statusService = appContext.getBean(RemoteStatusService.class);

            GetStatusResponse response = statusService.getStatus(new GetStatusRequest());

            for (StatusEntry statusEntry : response.getStatusEntryList()) {
                log.info(statusEntry.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
