package controller;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class ListUserController extends AbstractController {

    @Override
    public void doGet(final HttpRequest request, final HttpResponse response) throws IOException {
        final boolean logined = Boolean.parseBoolean(request.getCookie("logined"));
        if (!logined) {
            response.sendRedirect("/user/login.html");
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

        response.forwardBody(stringBuilder.toString());
    }
}
