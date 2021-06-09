package by.bsuir.messages;

import java.util.Map;

public class HttpMessage {
    protected final HttpMessageDTO httpMessageDTO;

    public HttpMessage(HttpMessageDTO httpMessageDTO) {
        this.httpMessageDTO = httpMessageDTO;
    }

    public boolean isClosed() {
        Map<String, String> headers = httpMessageDTO.getHeaders();
        String connection = headers.get("Connection");
        return (connection != null) && !connection.equals("keep-alive");
    }

    public String getHeader() {
        StringBuilder builder = new StringBuilder();
        // Первая строка
        builder.append(httpMessageDTO.getFirstLine()).append("\r\n");
        // Заголовки
        httpMessageDTO.getHeaders().forEach((key, value) -> builder.append(key).append(": ").append(value).append("\r\n"));
        // Пустая строка
        builder.append("\r\n");
        // Результат
        return builder.toString();
    }

    public byte[] getBody() {
        return httpMessageDTO.getBody();
    }
}
