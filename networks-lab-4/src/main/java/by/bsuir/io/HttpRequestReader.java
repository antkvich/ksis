package by.bsuir.io;

import by.bsuir.messages.HttpRequest;

import java.io.IOException;
import java.net.Socket;

public class HttpRequestReader extends HttpReader {

    public HttpRequestReader(Socket socket) throws IOException {
        super(socket);
    }

    public HttpRequest read() throws IOException {
        return new HttpRequest(readHttpMessageData());
    }
}
