package stroom.proxy.handler;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import stroom.proxy.repo.HeaderMap;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.StringTokenizer;

public class HeaderMapFactory {
    public HeaderMap create() {
        HeaderMap headerMap = new HeaderMap();

        HttpServletRequest httpServletRequest = getHttpServletRequest();
        addAllHeaders(httpServletRequest, headerMap);
        addAllQueryString(httpServletRequest, headerMap);

        return headerMap;
    }

    protected HttpServletRequest getHttpServletRequest() {
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        return httpServletRequest;
    }

    @SuppressWarnings("unchecked")
    private void addAllHeaders(HttpServletRequest httpServletRequest, HeaderMap headerMap) {
        Enumeration<String> headerNames = httpServletRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            headerMap.put(header, httpServletRequest.getHeader(header));
        }
    }

    private void addAllQueryString(HttpServletRequest httpServletRequest, HeaderMap headerMap) {
        String queryString = httpServletRequest.getQueryString();
        if (queryString != null) {
            StringTokenizer st = new StringTokenizer(httpServletRequest.getQueryString(), "&");
            while (st.hasMoreTokens()) {
                String pair = (String) st.nextToken();
                int pos = pair.indexOf('=');
                if (pos != -1) {
                    String key = pair.substring(0, pos);
                    String val = pair.substring(pos + 1, pair.length());

                    headerMap.put(key, val);
                }
            }
        }
    }

    public static HeaderMap cloneAllowable(final HeaderMap in) {
        final HeaderMap headerMap = new HeaderMap();
        headerMap.putAll(in);
        headerMap.removeAll(StroomHeaderArguments.HEADER_CLONE_EXCLUDE_SET);
        return headerMap;
    }
}
