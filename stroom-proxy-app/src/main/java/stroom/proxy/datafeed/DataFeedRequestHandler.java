package stroom.proxy.datafeed;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.HttpRequestHandler;

import stroom.proxy.handler.DropStreamException;
import stroom.proxy.handler.RequestHandler;
import stroom.proxy.util.ProxyProperties;
import stroom.proxy.util.io.CloseableUtil;
import stroom.proxy.util.logging.StroomLogger;
import stroom.proxy.util.shared.ModelStringUtil;
import stroom.proxy.util.thread.ThreadLocalBuffer;
import stroom.proxy.util.thread.ThreadScopeContextHolder;
import stroom.proxy.util.thread.ThreadScopeRunnable;
import stroom.proxy.util.zip.StroomStreamException;
import stroom.proxy.util.zip.StroomStreamProcessor;
import stroom.proxy.util.zip.HeaderMap;

/**
 * Main entry point to handling proxy requests.
 *
 * This class used the main spring context and forwards the request on to our
 * dynamic mini proxy.
 */
public class DataFeedRequestHandler implements HttpRequestHandler, InitializingBean {
    public static final String POST = "POST";

    private static StroomLogger LOGGER = StroomLogger.getLogger(DataFeedRequestHandler.class);

    @Resource
    HeaderMap headerMap;

    @Resource
    ProxyHandlerFactory proxyHandlerFactory;

    @Resource(name = "proxyRequestThreadLocalBuffer")
    ThreadLocalBuffer proxyRequestThreadLocalBuffer;

    private static AtomicInteger concurrentRequestCount = new AtomicInteger(0);

    private int returnCode = HttpServletResponse.SC_OK;

    HttpServletRequest httpServletRequest;
    HttpServletResponse httpServletResponse;

    @Override
    public void handleRequest(final HttpServletRequest na_request, final HttpServletResponse na_response)
            throws ServletException, IOException {
        httpServletRequest = na_request;
        httpServletResponse = na_response;

        if (!POST.equals(httpServletRequest.getMethod())) {
            httpServletResponse.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Only post supported");
            return;
        }

        new ThreadScopeRunnable() {
            @Override
            protected void exec() {
                ThreadScopeContextHolder.getContext().put(HeaderMap.NAME, headerMap);

                // Send the data
                final List<RequestHandler> handlers = proxyHandlerFactory.getIncomingRequestHandlerList();

                concurrentRequestCount.incrementAndGet();
                final long startTime = System.currentTimeMillis();

                try {
                    final StroomStreamProcessor stroomStreamProcessor = new StroomStreamProcessor(headerMap, handlers,
                            proxyRequestThreadLocalBuffer.getBuffer(), "DataFeedRequestHandler");

                    stroomStreamProcessor.processRequestHeader(httpServletRequest);

                    for (final RequestHandler requestHandler : handlers) {
                        requestHandler.handleHeader();
                    }

                    stroomStreamProcessor.process(httpServletRequest.getInputStream(), "");

                    for (final RequestHandler requestHandler : handlers) {
                        requestHandler.handleFooter();
                    }
                    httpServletResponse.setStatus(HttpServletResponse.SC_OK);
                } catch (final Exception s) {
                    try {
                        handleException(handlers, s, headerMap);
                    } catch (final IOException ioEx) {
                        throw new RuntimeException(ioEx);
                    }
                }

                if (LOGGER.isInfoEnabled()) {
                    final long time = System.currentTimeMillis() - startTime;
                    LOGGER.info("\"doPost() - Took %s to process (concurrentRequestCount=%s) %s\",%s",
                            ModelStringUtil.formatDurationString(time), concurrentRequestCount, returnCode,
                            CSVFormatter.format(headerMap));
                }
                concurrentRequestCount.decrementAndGet();

            }
        }.run();
    }

    /**
     * Let all the handlers have a go with the exception.
     */
    private void handleException(final List<RequestHandler> handlers, final Exception ex, final HeaderMap headerMap)
            throws IOException {
        boolean error = true;
        if (ex instanceof DropStreamException) {
            // Just read the stream in and ignore it
            final InputStream inputStream = httpServletRequest.getInputStream();
            while (inputStream.read(proxyRequestThreadLocalBuffer.getBuffer()) >= 0)
                ;
            CloseableUtil.close(inputStream);
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
            LOGGER.warn("\"handleException() - Dropped stream\",%s", CSVFormatter.format(headerMap));
            error = false;
        } else {
            if (ex instanceof StroomStreamException) {
                LOGGER.warn("\"handleException()\",%s,\"%s\"", CSVFormatter.format(headerMap),
                        CSVFormatter.escape(ex.getMessage()));
            } else {
                LOGGER.error("\"handleException()\",%s", CSVFormatter.format(headerMap), ex);
            }
        }
        for (final RequestHandler requestHandler : handlers) {
            try {
                requestHandler.handleError();
            } catch (final Exception ex1) {
                LOGGER.error("\"handleException\"", ex1);
            }
        }
        // Dropped stream errors are handled like all OK
        if (error) {
            returnCode = StroomStreamException.sendErrorResponse(httpServletResponse, ex);
        } else {
            httpServletResponse.setStatus(HttpServletResponse.SC_OK);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (!ProxyProperties.validate()) {
            throw new RuntimeException("Unable to start as properties are invalid");
        }
    }
}
