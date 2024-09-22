package controller;

import db.DataBase;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.HttpRequest;
import webserver.HttpResponse;

public class CreateUserController extends AbstractController {

    private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);

    @Override
    public void doPost(final HttpRequest httpRequest, final HttpResponse httpResponse) {
        final User user = new User(httpRequest.getBody("userId"),
                httpRequest.getBody("password"),
                httpRequest.getBody("name"),
                httpRequest.getBody("email")
        );
        DataBase.addUser(user);
        log.info("Signup successful: User: {}", user);

        httpResponse.sendRedirect("/index.html");
    }
}
