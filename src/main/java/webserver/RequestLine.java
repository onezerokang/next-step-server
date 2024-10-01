package webserver;

import util.HttpRequestUtils;

import java.util.HashMap;
import java.util.Map;

public class RequestLine {
    private static final String REQUEST_LINE_DELIMITER = " ";
    private static final String QUERYSTRING_DELIMITER = "?";

    private final HttpMethod method;
    private final String path;
    private final Map<String, String> querystring = new HashMap<>();

    public RequestLine(final String requestLine) {
        if (requestLine == null) {
            throw new IllegalArgumentException("Request line cannot be null");
        }

        final String[] requestLineTokens = requestLine.split(REQUEST_LINE_DELIMITER);
        this.method = HttpMethod.valueOf(requestLineTokens[0]);

        final String url = requestLineTokens[1];
        final int index = url.indexOf(QUERYSTRING_DELIMITER);
        this.path = index == -1 ? url : url.substring(0, index);
        if (index != -1) {
            this.querystring.putAll(HttpRequestUtils.parseQueryString(url.substring(index + 1)));
        }
    }

    public HttpMethod getMethod() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    public Map<String, String> getQuerystring() {
        return this.querystring;
    }
}
