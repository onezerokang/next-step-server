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

    private static final Map<String, Controller> controllers = new HashMap<String, Controller>() {{
        put("/user/create", new CreateUserController());
        put("/user/login", new LoginController());
        put("/user/list", new ListUserController());
    }};

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
        final Controller controller = controllers.get(httpRequest.getPath());
        if (controller == null) {
            httpResponse.forward(httpRequest.getPath());
            return;
        }
        controller.service(httpRequest, httpResponse);
    }
}
