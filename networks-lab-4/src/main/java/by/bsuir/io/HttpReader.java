package by.bsuir.io;

import by.bsuir.messages.HttpMessageDTO;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpReader {
    private final BufferedInputStream inputStream;

    public HttpReader(Socket socket) throws IOException {
        inputStream = new BufferedInputStream(socket.getInputStream());
    }

    protected final HttpMessageDTO readHttpMessageData() throws IOException {
        String firstLine = readFirstLine();
        Map<String, String> headers = new HashMap<>();
        int contentLength = readHeaders(headers);
        byte[] body = readBody(contentLength);
        return new HttpMessageDTO(firstLine, headers, body);
    }

    private String readFirstLine() throws IOException {
        return readLine();
    }

    private int readHeaders(Map<String, String> headers) throws IOException {
        int contentLength = -1;
        String line;
        while (!(line = readLine()).isEmpty()) {
            // Проверка наличия длины для тела
            if (line.startsWith("Content-Length")) {
                contentLength = Integer.parseInt(line.substring(16));
            }
            // Вставка
            int n = line.indexOf(':');
            String key = line.substring(0, n);
            String value = line.substring(n + 2);
            headers.put(key, value);
        }
        return contentLength;
    }

    private byte[] readBody(int contentLength) throws IOException {
        byte[] body = null;
        if (contentLength != -1) {
            body = inputStream.readNBytes(contentLength);
        }
        return body;
    }

    private String readLine() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int buffer;
        while (((buffer = inputStream.read()) != '\r') || ((buffer = inputStream.read()) != '\n')) {
            if (buffer != -1) {
                outputStream.write(buffer);
            } else {
                break;
            }
        }
        return outputStream.toString(StandardCharsets.US_ASCII);
    }
}
