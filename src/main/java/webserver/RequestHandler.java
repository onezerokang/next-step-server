package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import db.DataBase;
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
            handleHttpRequest(out, httpRequestMessage);
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

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, Map<String, String> cookies) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            for (String key : cookies.keySet()) {
                dos.writeBytes("Set-Cookie: " + key + "=" + cookies.get(key) + "\r\n");
            }
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, int lengthOfBodyContent, String location) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \r\n");
            dos.writeBytes("Location: " + location + "\r\n");
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

    private void handleHttpRequest(OutputStream out, HttpRequestMessage httpRequestMessage) throws IOException {
        log.info("{}:{}", httpRequestMessage.getUrl(), httpRequestMessage.getBody());
        DataOutputStream dos = new DataOutputStream(out);

        final Path path = new File("./webapp" + httpRequestMessage.getUrl()).toPath();
        if (Files.exists(path)) {
            final byte[] body = Files.readAllBytes(path);
            response200Header(dos, body.length);
            responseBody(dos, body);
        }

        if (httpRequestMessage.getRequestPath().equals("/user/create")) {
            signup(httpRequestMessage.getBody("userId"),
                    httpRequestMessage.getBody("password"),
                    httpRequestMessage.getBody("name"),
                    httpRequestMessage.getBody("email"));

            final byte[] body = new byte[0];
            response302Header(dos, body.length, "/index.html");
            responseBody(dos, body);
        } else if (httpRequestMessage.getRequestPath().equals("/user/login")) {
            final boolean isSuccess = login(httpRequestMessage.getBody("userId"), httpRequestMessage.getBody("password"));
            Map<String, String> cookies = new HashMap<>();
            cookies.put("logined", Boolean.toString(isSuccess));

            final byte[] body = new byte[0];
            response200Header(dos, body.length, cookies);
            responseBody(dos, body);
        }
    }

    private void signup(String userId, String password, String name, String email) {
        final User user = new User(userId, password, name, email);
        DataBase.addUser(user);
        log.info("Signup successful: User: {}", user);
    }

    private boolean login(String userId, String password) {
        final User user = DataBase.findUserById(userId);
        if (user == null) {
            return false;
        }
        return user.getPassword().equals(password);
    }
}
