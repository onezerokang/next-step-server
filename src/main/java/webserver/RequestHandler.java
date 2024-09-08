package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            final List<String> requestHeader = parseRequestHeader(in);
            final String url = requestHeader.get(0).split(" ")[1];
            byte[] body = getResponseBodyFromUrl(url);

            DataOutputStream dos = new DataOutputStream(out);
            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private List<String> parseRequestHeader(InputStream in) throws IOException {
        List<String> requestHeader = new ArrayList<>();
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

        String line;
        while (!"".equals(line = bufferedReader.readLine())) {
            if (line == null) {
                return requestHeader;
            }
            log.info(line);
            requestHeader.add(line);
        }
        return requestHeader;
    }

    private byte[] getResponseBodyFromUrl(String url) throws IOException {
        if ("/index.html".equals(url)) {
            return Files.readAllBytes(new File("./webapp" + url).toPath());
        } else {
            return "Hello World".getBytes();
        }
    }
}
