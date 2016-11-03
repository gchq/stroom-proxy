package stroom.proxy.util.web;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import stroom.proxy.util.cert.CertificateUtil;
import stroom.proxy.util.io.StreamUtil;

/**
 * Utility between Stroom and Stroom PROXY
 */
public final class DebugServletUtil {
    public static void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final StringBuilder debugResponse = new StringBuilder();

        debugResponse.append("\n");
        debugResponse.append("HTTP Header\n");
        debugResponse.append("===========\n");

        @SuppressWarnings("unchecked")
        Enumeration<String> headers = req.getHeaderNames();

        while (headers.hasMoreElements()) {
            String headerKey = headers.nextElement();
            String headerValue = req.getHeader(headerKey);
            debugResponse.append("[" + headerKey + "]=[" + headerValue + "]\n");
        }

        debugResponse.append("\n");
        debugResponse.append("HTTP Header\n");
        debugResponse.append("===========\n");
        debugResponse.append("contentLength=" + req.getContentLength());

        debugResponse.append("\n");

        debugResponse.append("HTTP Client Certificate\n");
        debugResponse.append("=======================\n");
        String certDn = CertificateUtil.extractCertificateDN(req);
        if (certDn != null) {
            debugResponse.append(certDn + "\n");
        } else {
            debugResponse.append("None\n");
        }
        debugResponse.append("\n");

        debugResponse.append("HTTP Payload\n");
        debugResponse.append("============\n");
        String payload = StreamUtil.streamToString(req.getInputStream());
        debugResponse.append(payload + "\n");

        debugResponse.append("\n");

        resp.getWriter().write(debugResponse.toString());
        resp.setStatus(HttpServletResponse.SC_OK);
    }

}
