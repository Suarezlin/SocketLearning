package server.component.handler;

import server.component.notifier.ClientNotifier;
import utils.SocketUtils;

import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private volatile boolean done = false;
    private final ClientNotifier clientNotifier;

    public ClientHandler(Socket socket, ClientNotifier clientNotifier) {
        this.socket = socket;
        this.clientNotifier = clientNotifier;
        System.out.println("新连接: " + socket.getInetAddress() + ":" + socket.getPort());
    }

    public void sendMessage(String msg) {
        SocketUtils.send(socket, msg);
    }

    public void close() {
        done = true;
        if (socket != null) {
            try {
                socket.close();
                System.out.println("断开连接: " + socket.getInetAddress() + ":" + socket.getPort());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        clientNotifier.notifyRemove(this);
    }

    @Override
    public void run() {
        sendMessage("已连接，请发送消息");
        while (!done) {
            SocketUtils.ReceiveMsg msg = SocketUtils.receive(socket);
            if (msg == null || !msg.isStatus()) {
                close();
            } else {
                System.out.println("收到来自 " + socket.getInetAddress() + ":" + socket.getPort() + " 的消息: " + msg.getMsg());
                clientNotifier.notifyBroadcast(socket.getInetAddress() + ":" + socket.getPort() + ": " + msg.getMsg());
            }
        }
    }
}
