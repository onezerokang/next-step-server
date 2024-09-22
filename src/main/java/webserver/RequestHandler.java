package webserver;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;

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
            final HttpResponse httpResponse = new HttpResponse(out);

            handleHttpRequest(out, httpRequest, httpResponse);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void handleHttpRequest(OutputStream out, HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        if (httpRequest.getPath().equals("/user/create")) {
            signup(httpRequest, httpResponse);
        } else if (httpRequest.getPath().equals("/user/login")) {
            login(httpRequest, httpResponse);
        } else if (httpRequest.getPath().equals("/user/list")) {
            getUserListPage(httpRequest, httpResponse);
        } else {
            httpResponse.forward(httpRequest.getPath());
        }
    }

    private void signup(final HttpRequest httpRequest, final HttpResponse httpResponse) {
        final User user = new User(httpRequest.getBody("userId"),
                httpRequest.getBody("password"),
                httpRequest.getBody("name"),
                httpRequest.getBody("email")
        );
        DataBase.addUser(user);
        log.info("Signup successful: User: {}", user);

        httpResponse.sendRedirect("/index.html");
    }

    private void login(final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException {
        final User user = DataBase.findUserById(httpRequest.getBody("userId"));
        if (user == null || !user.getPassword().equals(httpRequest.getBody("password"))) {
            httpResponse.addHeader("Set-Cookie", "logined=false");
            httpResponse.forward("/user/login_failed.html");
            return;
        }
        httpResponse.addHeader("Set-Cookie", "logined=true");
        httpResponse.sendRedirect("/index.html");
    }

    private void getUserListPage(final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException {
        final Map<String, String> cookie = HttpRequestUtils.parseCookies(httpRequest.getHeader("Cookie"));
        final boolean logined = Boolean.parseBoolean(cookie.get("logined"));
        if (!logined) {
            httpResponse.sendRedirect("/user/login.html");
            return;
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

        httpResponse.forward(stringBuilder.toString());
    }
}
