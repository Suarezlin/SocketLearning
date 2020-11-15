package client.component.handler;

import client.component.notifier.CloseNotifier;
import utils.ServerInfo;
import utils.SocketUtils;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Starter implements Runnable, CloseNotifier {

    private volatile Socket socket;
    private volatile boolean done = false;
    private volatile MessageHandler handler;
    private final ExecutorService handlerPool = Executors.newSingleThreadExecutor();
    private final CloseNotifier closeNotifier;
    private final ServerInfo serverInfo;


    public Starter(CloseNotifier closeNotifier, ServerInfo serverInfo) {
        this.closeNotifier = closeNotifier;
        this.serverInfo = serverInfo;
    }

    public void close() {
        if (handler != null) {
            handler.close();
        }
        notifyClose();
    }

    public void send(String msg) {
        SocketUtils.send(socket, msg);
    }

    @Override
    public void run() {
        try {
            socket = new Socket(serverInfo.getIp(), serverInfo.getPort());
            System.out.println("建立连接: " + socket.getLocalAddress() + ":" + socket.getLocalPort() + " -> " + socket.getInetAddress() + ":" + socket.getPort());
        } catch (Exception e) {
            e.printStackTrace();
            close();
            return;
        }
        handler = new MessageHandler(socket, this);
        handlerPool.submit(handler);
        while (!done) {
            SocketUtils.ReceiveMsg msg = SocketUtils.receive(socket);
            if (msg == null || !msg.isStatus()) {
                close();
                System.out.println("连接断开");
                return;
            } else {
                System.out.println("收到消息: " + msg.getMsg());
            }
        }
    }

    @Override
    public void notifyClose() {
        done = true;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        handlerPool.shutdownNow();
        closeNotifier.notifyClose();
    }
}
