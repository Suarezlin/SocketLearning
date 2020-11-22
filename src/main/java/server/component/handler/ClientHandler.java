package server.component.handler;

import core.Connector;
import server.component.notifier.ClientNotifier;
import utils.SocketUtils;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;

public class ClientHandler {

    private final SocketChannel channel;
    private volatile boolean done = false;
    private final ClientNotifier clientNotifier;
    private final Connector connector;

    public ClientHandler(SocketChannel channel, ClientNotifier clientNotifier) throws IOException {
        this.channel = channel;
        this.clientNotifier = clientNotifier;

        connector = new Connector() {
            @Override
            protected void onReceiveNewMessage(String str) {
                super.onReceiveNewMessage(str);
                clientNotifier.notifyBroadcast(str);
            }

            @Override
            public void onChannelClosed(SocketChannel channel) {
                super.onChannelClosed(channel);
                try {
                    close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
        connector.setup(channel);

    }

    public void send(String msg) {

    }

    public void close() {
        done = true;
        try {
            connector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
