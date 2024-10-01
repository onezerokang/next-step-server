import org.junit.Test;
import webserver.HttpMethod;
import webserver.HttpRequest;

import java.io.File;
import java.io.FileInputStream;

import static org.junit.Assert.assertEquals;

public class HttpRequestTest {
    private final String testDirectory = "./src/test/resources/";

    @Test
    public void request_GET() throws Exception {
        // given
        final FileInputStream in = new FileInputStream(new File(testDirectory + "Http_GET.txt"));

        // when
        final HttpRequest request = new HttpRequest(in);

        // then
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive", request.getHeader("Connection"));
        assertEquals("javajigi", request.getQuerystring("userId"));
    }

    @Test
    public void request_POST() throws Exception {
        // given
        final FileInputStream in = new FileInputStream(new File(testDirectory + "Http_POST.txt"));

        // when
        final HttpRequest request = new HttpRequest(in);

        // then
        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals("/user/create", request.getPath());
        assertEquals("keep-alive", request.getHeader("Connection"));
        assertEquals("javajigi", request.getBody("userId"));
    }
}
