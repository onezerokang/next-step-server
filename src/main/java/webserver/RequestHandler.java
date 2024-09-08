package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            final HttpRequestMessage httpRequestMessage = HttpRequestMessage.parseRequest(in);

            byte[] body = handleHttpRequest(httpRequestMessage);

            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private byte[] handleHttpRequest(HttpRequestMessage httpRequestMessage) throws IOException {
        final Path path = new File("./webapp" + httpRequestMessage.getUrl()).toPath();
        if (Files.exists(path)) {
            return Files.readAllBytes(path);
        }

        if (httpRequestMessage.getRequestPath().equals("/user/create")) {
            signup(httpRequestMessage.getBody("userId"),
                    httpRequestMessage.getBody("password"),
                    httpRequestMessage.getBody("name"),
                    httpRequestMessage.getBody("email"));
        }

        return "Hello, World!".getBytes();
    }

    private void signup(String userId, String password, String name, String email) {
        final User user = new User(userId, password, name, email);
        log.info("Signup successful: User: {}", user);
    }
}
