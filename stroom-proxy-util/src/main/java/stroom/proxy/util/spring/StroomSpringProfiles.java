package stroom.proxy.util.spring;

public final class StroomSpringProfiles {
    /**
     * Beans that should act in production only should have this profile.
     */
    public static final String PROD = "production";

    /**
     * Beans that should exist only in integration tests should have this
     * profile.
     *
     * Mostly TEST and PROD are appropriate profiles for the contexts, but in
     * one or two cases they are insufficient.
     */
    public static final String IT = "misc";

    /**
     * This is used wherever stroomCoreServerLocalTestingContext.xml is used.
     */
    public static final String TEST = "test";

    private StroomSpringProfiles() {
    }
}
