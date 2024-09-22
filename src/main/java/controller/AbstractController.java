package controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;

public abstract class AbstractController implements Controller {

    @Override
    public void service(final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException {
        if (httpRequest.getMethod().equals("GET")) {
            doGet(httpRequest, httpResponse);
        } else if (httpRequest.getMethod().equals("POST")) {
            doPost(httpRequest, httpResponse);
        }
    }

    public void doGet(final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException {
    }


    public void doPost(final HttpRequest httpRequest, final HttpResponse httpResponse) throws IOException {
    }
}
