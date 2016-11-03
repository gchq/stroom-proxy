package stroom.proxy.util.test;

import java.io.File;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import stroom.proxy.util.io.FileUtil;
import stroom.proxy.util.logging.StroomLogger;
import stroom.proxy.util.logging.LogExecutionTime;
import stroom.proxy.util.task.ExternalShutdownController;
import stroom.proxy.util.thread.ThreadScopeContextHolder;

public class StroomJUnit4ClassRunner extends BlockJUnit4ClassRunner {
    private final static StroomLogger LOGGER = StroomLogger.getLogger(StroomJUnit4ClassRunner.class);

    public StroomJUnit4ClassRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    public void run(final RunNotifier notifier) {
        try {
            TestState.getState().create();

            printTemp();
            super.run(notifier);
            printTemp();

        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void printTemp() {
        try {
            final File dir = FileUtil.getTempDir();
            if (dir != null) {
                System.out.println("TEMP DIR = " + dir.getCanonicalPath());
            } else {
                System.out.println("TEMP DIR = NULL");
            }
        } catch (final Exception e) {
        }
    }

    @Override
    protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
        try {
            try {
                ThreadScopeContextHolder.createContext();

                final LogExecutionTime logExecutionTime = new LogExecutionTime();
                try {
                    runChildBefore(this, method, notifier);
                    super.runChild(method, notifier);

                } finally {
                    runChildAfter(this, method, notifier, logExecutionTime);
                }

            } finally {
                ThreadScopeContextHolder.destroyContext();
            }
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        ExternalShutdownController.shutdown();
    }

    static void runChildBefore(final BlockJUnit4ClassRunner runner, final FrameworkMethod method,
            final RunNotifier notifier) {
        final StroomExpectedException stroomExpectedException = method.getAnnotation(StroomExpectedException.class);
        if (stroomExpectedException != null) {
            StroomJunitConsoleAppender.setExpectedException(stroomExpectedException.exception());
            LOGGER.info(">>> %s.  Expecting Exceptions %s", method.getMethod(), stroomExpectedException.exception());
        } else {
            LOGGER.info(">>> %s", method.getMethod());
        }

    }

    static void runChildAfter(final BlockJUnit4ClassRunner runner, final FrameworkMethod method,
            final RunNotifier notifier, final LogExecutionTime logExecutionTime) {
        LOGGER.info("<<< %s took %s", method.getMethod(), logExecutionTime);
        if (StroomJunitConsoleAppender.getUnexpectedExceptions().size() > 0) {
            notifier.fireTestFailure(new Failure(
                    Description.createTestDescription(runner.getTestClass().getJavaClass(), method.getName()),
                    StroomJunitConsoleAppender.getUnexpectedExceptions().get(0)));
        }
        StroomJunitConsoleAppender.setExpectedException(null);
    }
}
