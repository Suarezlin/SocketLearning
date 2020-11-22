package server.component.handler;

import core.IOContext;
import core.impl.IOSelectorProvider;
import server.component.notifier.ClientNotifier;
import server.component.notifier.CloseNotifier;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.*;

public class Server implements Runnable {

    private final int port;
    private final CloseNotifier closeNotifier;
    private final ClientNotifier clientNotifier;
    private volatile boolean done;
    private final ServerSocketChannel channel;
    private final Selector selector;

    private final ExecutorService handlerPool = Executors.newCachedThreadPool();

    public Server(int port, CloseNotifier closeNotifier, ClientNotifier clientNotifier) throws IOException {
        this.port = port;
        this.closeNotifier = closeNotifier;
        this.clientNotifier = clientNotifier;
        done = false;

        IOContext context = IOContext.setup().ioProvider(new IOSelectorProvider()).start();

        selector = Selector.open();
        channel = ServerSocketChannel.open();
        channel.bind(new InetSocketAddress(port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }


    public void close() {
        done = true;
        try {
            channel.close();
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        handlerPool.shutdown();
        IOContext.close();
    }

    @Override
    public void run() {
        System.out.println("服务器启动，端口: " + port);
        while (!done) {
            try {
                if (selector.select() != 0) {
                    continue;
                }
                for (SelectionKey key : selector.selectedKeys()) {
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                    if (key.isAcceptable()) {
                        SocketChannel socketChannel = channel.accept();
                        System.out.println("新连接: " + socketChannel.socket().getInetAddress() + ":" + socketChannel.socket().getPort());
                        ClientHandler clientHandler = new ClientHandler(socketChannel, clientNotifier);
                        clientNotifier.notifyAdd(clientHandler);
                    }
                }
            } catch (IOException ignored) {
            }
        }
    }
}
