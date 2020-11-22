package nio.component.handler;

import core.Connector;
import core.IOArgs;
import core.IOContext;
import core.IOProvider;
import core.impl.SocketChannelAdapter;
import nio.component.listener.ClientEventListener;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientHandler implements Closeable {

    private final SocketChannel channel;

    private final Connector connector;

    private final ClientEventListener clientEventListener;

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public ClientHandler(SocketChannel channel, ClientEventListener clientEventListener) throws IOException {
        this.channel = channel;
        this.clientEventListener = clientEventListener;
        connector = new Connector() {
            @Override
            public void onChannelClosed(SocketChannel channel) {
                super.onChannelClosed(channel);
                try {
                    close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clientEventListener.onClientClose(ClientHandler.this);
            }

            @Override
            protected void onReceiveNewMessage(String str) {
                System.out.println(ClientHandler.this + ": " + str);
            }
        };
        connector.setup(channel);
    }

    public String getChannelInfo() {
        return channel.socket().getInetAddress() + ":" + channel.socket().getPort();
    }

    @Override
    public String toString() {
        return getChannelInfo();
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            connector.close();
            channel.close();
        }
    }
}
