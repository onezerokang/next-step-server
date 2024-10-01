package webserver;

import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
    private static final String HEADER_DELIMITER = ": ";

    private final RequestLine requestLine;
    private final Map<String, String> headers;
    private final Map<String, String> body;

    public HttpRequest(final InputStream in) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

        String line = bufferedReader.readLine();

        if (line == null) {
            throw new IllegalArgumentException("Request line cannot be null");
        }

        this.requestLine = new RequestLine(line);

        final HashMap<String, String> headers = new HashMap<>();
        while (!"".equals(line = bufferedReader.readLine())) {
            if (line == null) {
                break;
            }
            final String[] headerTokens = line.split(HEADER_DELIMITER);
            headers.put(headerTokens[0], headerTokens[1]);
        }

        final HashMap<String, String> body = new HashMap<>();
        if (getMethod().isPost()) {
            final int contentLength = Integer.parseInt(headers.get("Content-Length"));
            body.putAll(HttpRequestUtils.parseQueryString(IOUtils.readData(bufferedReader, contentLength)));
        }

        this.headers = headers;
        this.body = body;
    }

    public HttpMethod getMethod() {
        return this.requestLine.getMethod();
    }

    public String getPath() {
        return this.requestLine.getPath();
    }

    public String getHeader(String key) {
        return this.headers.get(key);
    }

    public String getQuerystring(String key) {
        return this.requestLine.getQuerystring().get(key);
    }

    public String getBody(String key) {
        return this.body.get(key);
    }
}
