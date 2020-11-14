import utils.SocketUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Test {

    public static void main(String[] args) {
        new Thread(() -> {
            try {
                System.out.println("Server start");
                ServerSocket serverSocket = new ServerSocket(45678);
                Socket socket = serverSocket.accept();
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                SocketUtils.ReceiveMsg msg = null;
                do {
                    msg = SocketUtils.receive(socket);
                    if (msg != null) {
                        System.out.println(msg.getMsg());
                    }
                } while (msg != null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        try {
            Socket socket = new Socket("127.0.0.1", 45678);
            SocketUtils.send(socket, "你好");
            Thread.sleep(5000);
            SocketUtils.send(socket, "世界");
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
