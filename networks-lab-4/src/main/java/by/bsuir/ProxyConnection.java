package by.bsuir;

import by.bsuir.io.HttpRequestReader;
import by.bsuir.io.HttpResponseReader;
import by.bsuir.messages.HttpMessage;
import by.bsuir.messages.HttpRequest;
import by.bsuir.messages.HttpResponse;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.Queue;

public class ProxyConnection implements Closeable {
    private final Queue<String> urls = new LinkedList<>();
    private Socket clientSocket;
    private Thread clientThread;
    private Socket serverSocket;
    private Thread serverThread;

    public ProxyConnection(Socket clientSocket) {
        startClientConnection(clientSocket);
    }

    private void startClientConnection(Socket socket) {
        clientSocket = socket;
        clientThread = new Thread(this::serveClientRequests);
        clientThread.setDaemon(true);
        clientThread.start();
    }

    private void serveClientRequests() {
        try {
            HttpRequestReader httpRequestReader = new HttpRequestReader(clientSocket);
            BufferedOutputStream outputStream = null;
            while (!clientThread.isInterrupted()) {
                // Считываем клиентский запрос
                HttpRequest httpRequest = httpRequestReader.read();
                // Журналируем запрос
                String requestURL = httpRequest.getRequestURL();
                urls.add(requestURL);
                // Проверяем наличие соединения с сервером
                if (outputStream == null) {
                    startServerConnection(new URI(requestURL));
                    outputStream = new BufferedOutputStream(serverSocket.getOutputStream());
                }
                // Перенаправляем запрос серверу
                sendHttpMessage(outputStream, httpRequest);
                // Проверка необходимости закрыть соединение
                if (httpRequest.isClosed()) {
                    close();
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void startServerConnection(URI uri) throws IOException {
        int port = uri.getPort();
        serverSocket = new Socket(uri.getHost(), port != -1 ? port : 80);
        serverThread = new Thread(this::serveServerResponses);
        serverThread.setDaemon(true);
        serverThread.start();
    }

    private void serveServerResponses() {
        try {
            HttpResponseReader httpResponseReader = new HttpResponseReader(serverSocket);
            BufferedOutputStream outputStream = new BufferedOutputStream(clientSocket.getOutputStream());
            while (!serverThread.isInterrupted()) {
                // Считываем серверный ответ
                HttpResponse httpResponse = httpResponseReader.read();
                // Журналируем ответ
                log(httpResponse);
                // Перенаправляем ответ клиенту
                sendHttpMessage(outputStream, httpResponse);
                // Проверка необходимости закрыть соединение
                if (httpResponse.isClosed()) {
                    close();
                }
            }
        } catch (Exception ignored) {
        }
    }

    private void sendHttpMessage(BufferedOutputStream outputStream, HttpMessage httpMessage) throws IOException {
        // Запись заголовка
        byte[] headerBytes = httpMessage.getHeader().getBytes(StandardCharsets.US_ASCII);
        outputStream.write(headerBytes);
        // Запись тела
        byte[] bodyBytes = httpMessage.getBody();
        if (bodyBytes != null) {
            outputStream.write(bodyBytes);
        }
        // Запись буфера
        outputStream.flush();
    }

    private void log(HttpResponse httpResponse) {
        System.out.println(urls.poll() + ": " + httpResponse.getStatusCode());
    }

    @Override
    public void close() throws IOException {
        clientThread.interrupt();
        clientSocket.close();
        serverThread.interrupt();
        serverSocket.close();
    }
}
