package stroom.proxy.util.test;

import java.io.File;

import stroom.proxy.util.config.StroomProperties;
import stroom.proxy.util.io.FileUtil;

public class TestState {
    public static class State {
        private File testDir;
        private int classTestCount;
        private int threadTestCount;
        private boolean doneSetup;

        public void create() {
            try {
                if (testDir == null) {
                    final File initialTempDir = FileUtil.getInitialTempDir();
                    final File rootTestDir = StroomTestUtil.createRootTestDir(initialTempDir);
                    testDir = StroomTestUtil.createPerThreadTestDir(rootTestDir);

                    // Redirect the temp dir for the tests.
                    StroomProperties.setOverrideProperty(StroomProperties.STROOM_TEMP, testDir.getCanonicalPath(), "test");

                    FileUtil.forgetTempDir();

                    // Let tests update the database
                    StroomProperties.setOverrideProperty("stroom.jpaHbm2DdlAuto", "update", "test");
                    StroomProperties.setOverrideProperty("stroom.connectionTesterClassName",
                            "stroom.entity.server.util.StroomConnectionTesterOkOnException", "test");
                }
            } catch (final Throwable t) {
                t.printStackTrace(System.err);
                throw new RuntimeException(t.getMessage(), t);
            }
        }

        public void destroy() {
            try {
                if (testDir != null) {
                    FileUtil.forceDelete(testDir);
                    FileUtil.forgetTempDir();
                    StroomProperties.removeOverrides();
                }
            } catch (final Throwable t) {
                t.printStackTrace(System.err);
                throw new RuntimeException(t.getMessage(), t);
            } finally {
                testDir = null;
            }
        }

        public void reset() {
            classTestCount = 0;
            doneSetup = false;
        }

        public void incrementTestCount() {
            classTestCount++;
            threadTestCount++;
        }

        public boolean isDoneSetup() {
            return doneSetup;
        }

        public void setDoneSetup(final boolean doneSetup) {
            this.doneSetup = doneSetup;
        }

        public int getClassTestCount() {
            return classTestCount;
        }

        @Override
        protected void finalize() throws Throwable {
            destroy();
            super.finalize();
        }
    }

    /**
     * Record and modify the test state in a static thread local as we want to
     * reset in static beforeClass() method.
     */
    private static ThreadLocal<State> stateThreadLocal = new ThreadLocal<>();

    public static State getState() {
        State state = stateThreadLocal.get();
        if (state == null) {
            state = new State();
            state.create();
            stateThreadLocal.set(state);
        }
        return state;
    }
}
