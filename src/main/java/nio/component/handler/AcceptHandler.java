package nio.component.handler;

import nio.component.listener.ClientEventListener;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class AcceptHandler implements Runnable, Closeable {

    private final int port;

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private final ServerSocketChannel channel;

    private final ClientEventListener clientEventListener;

    private final Selector selector;

    public AcceptHandler(int port, ClientEventListener clientEventListener) throws IOException {
        this.port = port;
        this.clientEventListener = clientEventListener;
        selector = Selector.open();
        channel = ServerSocketChannel.open();
        channel.bind(new InetSocketAddress(port));
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        System.out.println("服务器启动，监听: " + port);
        while (!isClosed.get()) {
            try {
                if (selector.select() == 0) {
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey key : selectionKeys) {

                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        ClientHandler clientHandler = new ClientHandler(socketChannel, clientEventListener);
                        clientEventListener.onClientCreate(clientHandler);
                    }
                }
                selectionKeys.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            selector.wakeup();
            selector.close();
            channel.close();
        }
    }
}
