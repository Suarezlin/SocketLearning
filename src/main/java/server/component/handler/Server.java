package server.component.handler;

import server.component.notifier.ClientNotifier;
import server.component.notifier.CloseNotifier;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

public class Server implements Runnable {

    private final int port;
    private final CloseNotifier closeNotifier;
    private final ClientNotifier clientNotifier;
    private volatile boolean done;
    private volatile ServerSocket serverSocket;

    private final ExecutorService handlerPool = Executors.newCachedThreadPool();

    public Server(int port, CloseNotifier closeNotifier, ClientNotifier clientNotifier) {
        this.port = port;
        this.closeNotifier = closeNotifier;
        this.clientNotifier = clientNotifier;
        done = false;
    }


    public void close() {
        done = true;
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        closeNotifier.notifyClose();
        handlerPool.shutdown();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("服务器启动，端口: " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!done) {
            try {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, clientNotifier);
                handlerPool.submit(clientHandler);
                clientNotifier.notifyAdd(clientHandler);
            } catch (IOException ignored) {
            }
        }
    }
}
