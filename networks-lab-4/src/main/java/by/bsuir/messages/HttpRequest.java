package by.bsuir.messages;

public class HttpRequest extends HttpMessage {

    public HttpRequest(HttpMessageDTO httpMessageDTO) {
        super(httpMessageDTO);
    }

    public String getRequestURL() {
        String startLine = httpMessageDTO.getFirstLine();
        String[] split = startLine.split(" ");
        return split[1];
    }
}
