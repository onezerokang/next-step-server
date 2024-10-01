import org.junit.Test;
import webserver.HttpMethod;
import webserver.RequestLine;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class RequestLineTest {

    @Test
    public void create_method() {
        final RequestLine requestLine = new RequestLine("GET /index.html HTTP/1.1");

        assertEquals(HttpMethod.GET, requestLine.getMethod());
        assertEquals("/index.html", requestLine.getPath());
    }

    @Test
    public void create_path_and_params() {
        final RequestLine requestLine = new RequestLine("GET /user/create?userId=javajigi&password=pass HTTP/1.1");

        assertEquals(HttpMethod.GET, requestLine.getMethod());
        assertEquals("/user/create", requestLine.getPath());
        final Map<String, String> params = requestLine.getQuerystring();
        assertEquals(2, params.size());
    }
}
