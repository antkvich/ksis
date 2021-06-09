package by.bsuir;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class ProxyServer {
    public static final int PORT = 53138;
    private final ServerSocket proxyServerSocket;
    private final Thread proxyServerThread;
    private final Set<ProxyConnection> proxyConnections = Collections.synchronizedSet(new LinkedHashSet<>());

    public ProxyServer() throws IOException {
        proxyServerSocket = new ServerSocket(PORT);
        proxyServerThread = new Thread(this::listen);
    }

    public void start() {
        proxyServerThread.start();
    }

    private void listen() {
        try {
            while (!proxyServerThread.isInterrupted()) {
                // Получение клиентского сокета
                Socket socket = proxyServerSocket.accept();
                // Создание прокси-соединения
                proxyConnections.add(new ProxyConnection(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
