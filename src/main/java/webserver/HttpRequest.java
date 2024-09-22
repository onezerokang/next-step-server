package webserver;

import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final String REQUEST_LINE_DELIMITER = " ";
    private static final String QUERYSTRING_DELIMITER = "?";
    private static final String HEADER_DELIMITER = ": ";

    private final String method;
    private final String path;
    private final Map<String, String> querystring;
    private final Map<String, String> headers;
    private final Map<String, String> body;

    public HttpRequest(final InputStream in) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

        String line = bufferedReader.readLine();

        final String[] requestLineTokens = line.split(REQUEST_LINE_DELIMITER);
        final String method = requestLineTokens[0];
        final String url = requestLineTokens[1];

        final int index = url.indexOf(QUERYSTRING_DELIMITER);
        final String path = index == -1 ? url : url.substring(0, index);
        final Map<String, String> querystring = index != -1 ?
                HttpRequestUtils.parseQueryString(url.substring(index + 1)) :
                new HashMap<>();

        final HashMap<String, String> headers = new HashMap<>();
        while (!"".equals(line = bufferedReader.readLine())) {
            if (line == null) {
                break;
            }
            final String[] headerTokens = line.split(HEADER_DELIMITER);
            headers.put(headerTokens[0], headerTokens[1]);
        }

        final HashMap<String, String> body = new HashMap<>();
        if ("POST".equals(method)) {
            final int contentLength = Integer.parseInt(headers.get("Content-Length"));
            body.putAll(HttpRequestUtils.parseQueryString(IOUtils.readData(bufferedReader, contentLength)));
        }

        this.method = method;
        this.path = path;
        this.querystring = querystring;
        this.headers = headers;
        this.body = body;
    }

    public String getMethod() {
        return this.method;
    }

    public String getPath() {
        return this.path;
    }

    public String getHeader(String key) {
        return this.headers.get(key);
    }

    public String getParameter(String key) {
        return this.method.equals("GET") ? this.querystring.get(key) : this.body.get(key);
    }

    public String getBody(String key) {
        return this.body.get(key);
    }

    @Override
    public String toString() {
        return "HttpRequestMessage{" +
                "method='" + method + '\'' +
                ", requestPath='" + path + '\'' +
                ", querystring=" + querystring +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }
}
