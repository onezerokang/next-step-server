package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpResponse {

    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);

    private final DataOutputStream dos;

    private final Map<String, String> headers = new HashMap<>();

    public HttpResponse(final OutputStream outputStream) {
        this.dos = new DataOutputStream(outputStream);
    }

    public void addHeader(final String key, final String value) {
        this.headers.put(key, value);
    }

    public void forward(final String requestPath) throws IOException {
        final Path filePath = new File("./webapp" + requestPath).toPath();
        final byte[] body = Files.readAllBytes(filePath);

        if (requestPath.endsWith(".css")) {
            headers.put("Content-Type", "text/css");
        } else if (requestPath.endsWith(".js")){
            headers.put("Content-Type", "application/javascript");
        } else {
            headers.put("Content-Type", "text/html;charset=utf-8");
        }
        headers.put("Content-Length", body.length + "");
        response200Header();
        responseBody(body);
    }

    public void forwardBody(final String body) {
        final byte[] contents = body.getBytes();
        headers.put("Content-Type", "text/html;charset-utf-8");
        headers.put("Content-Length", contents.length + "");
        response200Header();
        responseBody(contents);
    }

    public void sendRedirect(final String location) {
        try {
            this.dos.writeBytes("HTTP/1.1 302 Found \r\n");
            this.dos.writeBytes("Location: " + location + "\r\n");
            processHeaders();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void response200Header() {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            processHeaders();
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void responseBody(byte[] body) {
        try {
            this.dos.write(body, 0, body.length);
            this.dos.writeBytes("\r\n");
            this.dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void processHeaders() {
        final Set<String> keys = this.headers.keySet();
        for (String key : keys) {
            try {
                this.dos.writeBytes(key + ": " + this.headers.get(key) + "\r\n");
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }
}
