package webserver;

import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class HttpRequestMessage {
    private final String method;
    private final String url;
    private final String requestPath;
    private final Map<String, String> querystring;
    private final Map<String, String> headers;
    private final Map<String, String> body;

    public HttpRequestMessage(final String method,
                              final String url,
                              final String requestPath,
                              final Map<String, String> querystring,
                              final Map<String, String> headers,
                              final Map<String, String> body) {
        this.method = method;
        this.url = url;
        this.requestPath = requestPath;
        this.querystring = querystring;
        this.headers = headers;
        this.body = body;
    }

    public static HttpRequestMessage parseRequest(final InputStream requestInputStream) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(requestInputStream));

        String line = bufferedReader.readLine();

        final String[] startLineTokens = line.split(" ");
        final String method = startLineTokens[0];
        final String url = startLineTokens[1];

        final int index = url.indexOf("?");
        final String requestPath = index == -1 ? url : url.substring(0, index);
        final Map<String, String> queryString = index == -1 ?
                new HashMap<>() :
                HttpRequestUtils.parseQueryString(url.substring(index + 1));

        final HashMap<String, String> headers = new HashMap<>();
        while (!"".equals(line = bufferedReader.readLine())) {
            if (line == null) {
                break;
            }
            final String[] headerTokens = line.split(": ");
            headers.put(headerTokens[0], headerTokens[1]);
        }

        final HashMap<String, String> body = new HashMap<>();
        if ("POST".equals(method)) {
            final int contentLength = Integer.parseInt(headers.get("Content-Length"));
            body.putAll(HttpRequestUtils.parseQueryString(IOUtils.readData(bufferedReader, contentLength)));
        }

        return new HttpRequestMessage(method, url, requestPath, queryString, headers, body);
    }

    public String getUrl() {
        return this.url;
    }

    public String getRequestPath() {
        return this.requestPath;
    }

    public String getBody(String key) {
        return this.body.get(key);
    }

    @Override
    public String toString() {
        return "HttpRequestMessage{" +
                "method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", requestPath='" + requestPath + '\'' +
                ", querystring=" + querystring +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }
}
