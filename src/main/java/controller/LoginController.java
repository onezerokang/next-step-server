package controller;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;

public class LoginController extends AbstractController {

    @Override
    public void doPost(final HttpRequest request, final HttpResponse response) throws IOException {
        final User user = DataBase.findUserById(request.getBody("userId"));
        if (user == null || !user.getPassword().equals(request.getBody("password"))) {
            response.addHeader("Set-Cookie", "logined=false");
            response.forward("/user/login_failed.html");
            return;
        }
        response.addHeader("Set-Cookie", "logined=true");
        response.sendRedirect("/index.html");
    }
}
