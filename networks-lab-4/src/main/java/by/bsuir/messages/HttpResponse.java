package by.bsuir.messages;

public class HttpResponse extends HttpMessage {

    public HttpResponse(HttpMessageDTO httpMessageDTO) {
        super(httpMessageDTO);
    }

    public int getStatusCode() {
        String statusLine = httpMessageDTO.getFirstLine();
        String[] split = statusLine.split(" ");
        return Integer.parseInt(split[1]);
    }
}
