package stroom.proxy.repo;

import java.io.Serializable;

public interface Monitor extends HasTerminateHandlers, HasTerminate, HasInfo, Serializable {
    Monitor getParent();
}
