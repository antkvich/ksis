package by.bsuir.io;

import by.bsuir.messages.HttpResponse;

import java.io.IOException;
import java.net.Socket;

public class HttpResponseReader extends HttpReader {

    public HttpResponseReader(Socket socket) throws IOException {
        super(socket);
    }

    public HttpResponse read() throws IOException {
        return new HttpResponse(readHttpMessageData());
    }
}
