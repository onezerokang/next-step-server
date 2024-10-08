package webserver;

import controller.Controller;
import controller.CreateUserController;
import controller.ListUserController;
import controller.LoginController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
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

            handleHttpRequest(httpRequest, httpResponse);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void handleHttpRequest(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        final Controller controller = RequestMapping.getController(httpRequest.getPath());
        if (controller == null) {
            final String path = getDefaultPath(httpRequest.getPath());
            httpResponse.forward(path);
        } else {
            controller.service(httpRequest, httpResponse);
        }
    }

    private String getDefaultPath(String path) {
        if (path.equals("/")) {
            return "/index.html";
        }
        return path;
    }
}
