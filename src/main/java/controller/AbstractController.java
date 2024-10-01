package controller;

import webserver.HttpMethod;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;

public abstract class AbstractController implements Controller {

    @Override
    public void service(final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException {
        if (httpRequest.getMethod().isGet()) {
            doGet(httpRequest, httpResponse);
        } else if (httpRequest.getMethod().isPost()) {
            doPost(httpRequest, httpResponse);
        }
    }

    public void doGet(final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException {
    }


    public void doPost(final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException {
    }
}
