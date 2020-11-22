package server.component;

import server.component.handler.ClientHandler;
import server.component.notifier.ClientNotifier;
import server.component.notifier.CloseNotifier;
import server.component.handler.Server;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer implements CloseNotifier, ClientNotifier {

    private volatile ExecutorService acceptorPool;

    private volatile Server server;

    private final List<ClientHandler> handlers = new CopyOnWriteArrayList<>();

    private volatile ExecutorService broadcastPool;

    private final int port;

    public TCPServer(int port) {
        this.port = port;
    }

    public synchronized void start() {
        stop();
        try {
            server = new Server(port, this, this);
        } catch (IOException e) {
            System.out.println("启动失败: " + e.getMessage());
            return;
        }
        acceptorPool = Executors.newSingleThreadExecutor();
//        broadcastPool = Executors.newFixedThreadPool(16);
        acceptorPool.submit(server);
    }

    public synchronized void stop() {
        if (server != null) {
            server.close();
        }
        notifyClose();
    }

    public void broadcast(String msg) {
        notifyBroadcast(msg);
    }


    @Override
    public void notifyClose() {
        if (acceptorPool != null) {
            acceptorPool.shutdown();
            acceptorPool = null;
        }
        if (broadcastPool != null) {
            broadcastPool.shutdown();
        }

        for (ClientHandler handler : handlers) {
            handler.close();
        }
        handlers.clear();
    }

    @Override
    public void notifyAdd(ClientHandler clientHandler) {
        handlers.add(clientHandler);
    }

    @Override
    public void notifyRemove(ClientHandler clientHandler) {
        handlers.remove(clientHandler);
    }

    @Override
    public void notifyBroadcast(String msg) {
        System.out.println("广播: " + msg);
//        broadcastPool.submit(() -> {
//            handlers.forEach(handler -> handler.sendMessage(msg));
//        });

    }
}
