package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

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
            final HttpRequest httpRequest = new HttpRequest(in);
            handleHttpRequest(out, httpRequest);
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

    private void response200HeaderWithCss(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
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

    private void handleHttpRequest(OutputStream out, HttpRequest httpRequest) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);

        final Path path = new File("./webapp" + httpRequest.getPath()).toPath();
        if (Files.exists(path)) {
            final boolean isCssAccepted = httpRequest.getHeader("Accept").contains("text/css");
            final byte[] body = Files.readAllBytes(path);
            if (isCssAccepted) {
                response200HeaderWithCss(dos, body.length);
            } else {
                response200Header(dos, body.length);
            }
            responseBody(dos, body);
        }

        if (httpRequest.getPath().equals("/user/create")) {
            signup(httpRequest.getBody("userId"),
                    httpRequest.getBody("password"),
                    httpRequest.getBody("name"),
                    httpRequest.getBody("email"));

            final byte[] body = new byte[0];
            response302Header(dos, body.length, "/index.html");
            responseBody(dos, body);
        } else if (httpRequest.getPath().equals("/user/login")) {
            final boolean isSuccess = login(httpRequest.getBody("userId"), httpRequest.getBody("password"));
            Map<String, String> cookies = new HashMap<>();
            cookies.put("logined", Boolean.toString(isSuccess));

            final byte[] body = new byte[0];
            response200Header(dos, body.length, cookies);
            responseBody(dos, body);
        } else if (httpRequest.getPath().equals("/user/list")) {
            getUserListPage(httpRequest, dos);
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

    private void getUserListPage(final HttpRequest httpRequest, final DataOutputStream dos) {
        final Map<String, String> cookie = HttpRequestUtils.parseCookies(httpRequest.getHeader("Cookie"));
        final boolean logined = Boolean.parseBoolean(cookie.get("logined"));
        if (!logined) {
            final byte[] body = new byte[0];
            responseBody(dos, body);
            response302Header(dos, body.length, "/user/login.html");
        }

        final Collection<User> users = DataBase.findAll();

        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<ul>");
        for (User user : users) {
            stringBuilder.append("<li>");
            stringBuilder.append(user.toString());
            stringBuilder.append("</li>");
        }
        stringBuilder.append("</ul>");
        final byte[] body = stringBuilder.toString().getBytes();
        response200Header(dos, body.length);
        responseBody(dos, body);
    }
}
