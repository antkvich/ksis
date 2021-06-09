package by.bsuir.messages;

import java.util.Map;

public class HttpMessageDTO {
    private final String firstLine;
    private final Map<String, String> headers;
    private final byte[] body;

    public HttpMessageDTO(String firstLine, Map<String, String> headers, byte[] body) {
        this.firstLine = firstLine;
        this.headers = headers;
        this.body = body;
    }

    public String getFirstLine() {
        return firstLine;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public byte[] getBody() {
        return body;
    }
}
