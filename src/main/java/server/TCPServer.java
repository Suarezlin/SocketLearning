package server;

import utils.SocketUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPServer {

    private static ExecutorService acceptorPool;

    private static Server server;

    public synchronized static void start(int port) {
        stop();
        try {
            server = new Server(port);
            acceptorPool = Executors.newSingleThreadExecutor();
            acceptorPool.submit(server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void stop() {
        if (server != null) {
            server.close();
        }
        if (acceptorPool != null) {
            acceptorPool.shutdownNow();
            acceptorPool = null;
        }

    }

    private static class Server implements Runnable {

        private final int port;
        private volatile boolean done;
        private volatile ServerSocket serverSocket;

        private final ExecutorService handlerPool = Executors.newFixedThreadPool(50);

        private Server(int port) throws IOException {
            this.port = port;
            done = false;
        }

        public void close() {
            done = true;
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            handlerPool.shutdownNow();
        }

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("服务器启动，端口: " + port);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!done) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientHandler clientHandler = new ClientHandler(socket);
                    handlerPool.submit(clientHandler);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static class ClientHandler implements Runnable {

        private final Socket socket;
        private volatile boolean done = false;

        private ClientHandler(Socket socket) {
            this.socket = socket;
            System.out.println("新连接: " + socket.getInetAddress() + ":" + socket.getPort());
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
        }

        @Override
        public void run() {
            while (!done) {
                SocketUtils.ReceiveMsg msg = SocketUtils.receive(socket);
                if (msg == null || !msg.isStatus()) {
                    close();
                } else {
                    System.out.println("收到来自 " + socket.getInetAddress() + ":" + socket.getPort() + " 的消息: " + msg.getMsg());
                }
            }
        }
    }

    public static void main(String[] args) {
        start(12345);
    }

}
