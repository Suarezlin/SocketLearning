package nio.component;

import core.IOContext;
import core.impl.IOSelectorProvider;
import nio.component.handler.AcceptHandler;
import nio.component.handler.ClientHandler;
import nio.component.listener.ClientEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class TCPServer implements ClientEventListener {

    private volatile int port;

    private final List<ClientHandler> clientHandlers = new CopyOnWriteArrayList<>();

    private volatile AcceptHandler acceptHandler;

    private volatile ExecutorService acceptorPool;

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public synchronized void start(int port) {
        if (!isClosed.get()) {
            stop();
        }
        isClosed.set(false);
        try {
            IOContext.setup().ioProvider(new IOSelectorProvider()).start();
            acceptHandler = new AcceptHandler(port, this);
            acceptorPool = Executors.newSingleThreadExecutor();
            acceptorPool.submit(acceptHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public synchronized void stop() {
        if (isClosed.compareAndSet(false, true)) {
            try {
                if (acceptHandler != null) {
                    acceptHandler.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (acceptorPool != null) {
                acceptorPool.shutdown();
            }
            for (ClientHandler clientHandler : clientHandlers) {
                try {
                    clientHandler.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            clientHandlers.clear();
            IOContext.close();
        }
//        System.out.println("服务器停止");
    }

    @Override
    public void onClientClose(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        System.out.println("连接退出: " + clientHandler);
    }

    @Override
    public void onClientCreate(ClientHandler clientHandler) {
        clientHandlers.add(clientHandler);
        System.out.println("新建连接: " + clientHandler);
    }

    public static void main(String[] args) throws IOException {
        TCPServer server = new TCPServer();
        server.start(4567);
        System.in.read();
        server.stop();
    }
}
