package stroom.proxy.status.server;

import stroom.proxy.status.remote.GetStatusRequest;
import stroom.proxy.status.remote.GetStatusResponse;
import stroom.proxy.status.remote.GetStatusResponse.Status;
import stroom.proxy.status.remote.GetStatusResponse.StatusEntry;
import stroom.proxy.status.remote.RemoteStatusService;

public class RemoteStatusServiceImpl implements RemoteStatusService {
    @Override
    public GetStatusResponse getStatus(GetStatusRequest request) {
        GetStatusResponse response = new GetStatusResponse();
        response.getStatusEntryList().add(new StatusEntry(Status.Info, "SYSTEM", "All Ok"));
        return response;
    }

}
