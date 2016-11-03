package stroom.proxy.util.scheduler;

public class MalformedCronException extends RuntimeException {
    private static final long serialVersionUID = -7073838751104201047L;

    public MalformedCronException(final String message) {
        super(message);
    }
}
