package stroom.proxy.util.test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

public class StroomJunitConsoleAppender extends ConsoleAppender {
    private static Set<Class<? extends Throwable>> expectedExceptionSet;
    private static List<Throwable> unexpectedExceptions = new ArrayList<Throwable>();

    public static void setExpectedException(Class<? extends Throwable>[] expectedException) {
        expectedExceptionSet = new HashSet<>();
        unexpectedExceptions = new ArrayList<>();
        if (expectedException != null) {
            for (Class<? extends Throwable> ex : expectedException) {
                expectedExceptionSet.add(ex);
            }
        }
    }

    public static List<Throwable> getUnexpectedExceptions() {
        return unexpectedExceptions;
    }

    @Override
    public synchronized void doAppend(LoggingEvent event) {
        if (event.getThrowableInformation() != null) {
            Throwable throwable = event.getThrowableInformation().getThrowable();
            if (expectedExceptionSet == null || !expectedExceptionSet.contains(throwable.getClass())) {
                unexpectedExceptions.add(throwable);
            } else {
                // Ignore the expected exception
                event = new LoggingEvent(event.getFQNOfLoggerClass(), event.getLogger(), event.getTimeStamp(),
                        Level.DEBUG, "Ignore Exception - " + throwable.getMessage() + " - " + event.getMessage(), null);
            }
        }
        super.doAppend(event);
    }

}
