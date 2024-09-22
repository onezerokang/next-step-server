package controller;

import db.DataBase;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;

public class LoginController extends AbstractController {

    @Override
    public void doPost(final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException {
        final User user = DataBase.findUserById(httpRequest.getBody("userId"));
        if (user == null || !user.getPassword().equals(httpRequest.getBody("password"))) {
            httpResponse.addHeader("Set-Cookie", "logined=false");
            httpResponse.forward("/user/login_failed.html");
            return;
        }
        httpResponse.addHeader("Set-Cookie", "logined=true");
        httpResponse.sendRedirect("/index.html");
    }
}
