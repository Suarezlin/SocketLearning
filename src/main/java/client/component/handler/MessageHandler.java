package client.component.handler;

import client.component.notifier.CloseNotifier;
import utils.SocketUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class MessageHandler implements Runnable {

    private final Socket socket;
    private final CloseNotifier closeNotifier;
    private volatile boolean done = false;
    private volatile InputStream inputStream;

    public MessageHandler(Socket socket, CloseNotifier closeNotifier) {
        this.socket = socket;
        this.closeNotifier = closeNotifier;
    }

    public void close() {
        done = true;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        closeNotifier.notifyClose();
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        inputStream = System.in;
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        try {
            while (!done) {
                String input = reader.readLine();
                if (input == null) {
                    close();
                    return;
                }
                if ("%%%exit000".equals(input)) {
                    close();
                    System.out.println("退出");
                    return;
                } else {
                    SocketUtils.send(socket, input);
                }
            }
        } catch (Exception ignore) {
            close();
        }
    }
}
