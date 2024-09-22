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

public class HttpResponse {

    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);

    private final DataOutputStream dos;

    private final Map<String, String> header = new HashMap<>();

    public HttpResponse(final OutputStream outputStream) {
        this.dos = new DataOutputStream(outputStream);
    }

    public void forward(final String requestPath) throws IOException {
        final Path filePath = new File("./webapp" + requestPath).toPath();

        if (!Files.exists(filePath)) {
            response200Header(dos, requestPath.length());
            responseBody(dos, requestPath.getBytes());
            return;
        }

        final byte[] body = Files.readAllBytes(filePath);

        if (requestPath.endsWith(".css")) {
            response200HeaderWithCss(dos, body.length);
        } else {
            response200Header(dos, body.length);
        }

        responseBody(dos, body);
    }

    public void sendRedirect(final String location) {
        response302Header(dos, location);
    }

    public void addHeader(final String key, final String value) {
        this.header.put(key, value);
    }

    public void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            for (final String key: this.header.keySet()) {
                dos.writeBytes(key + ": " + this.header.get(key) + "\r\n");
            }
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200HeaderWithCss(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            for (final String key: this.header.keySet()) {
                dos.writeBytes(key + ": " + this.header.get(key) + "\r\n");
            }
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + location + "\r\n");
            for (final String key: this.header.keySet()) {
                dos.writeBytes(key + ": " + this.header.get(key) + "\r\n");
            }
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
