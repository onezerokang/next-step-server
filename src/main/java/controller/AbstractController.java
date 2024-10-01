package controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;

public abstract class AbstractController implements Controller {

    @Override
    public void service(final HttpRequest request, final HttpResponse httpResponse) throws IOException {
        if (request.getMethod().isGet()) {
            doGet(request, httpResponse);
        } else if (request.getMethod().isPost()) {
            doPost(request, httpResponse);
        }
    }

    protected void doGet(final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException {
    }


    protected void doPost(final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException {
    }
}
