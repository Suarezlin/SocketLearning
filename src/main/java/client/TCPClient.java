package client;

import utils.ServerInfo;
import utils.SocketUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TCPClient {

    private volatile Starter starter;
    private final ServerInfo serverInfo;
    private volatile ExecutorService starterPool;

    public TCPClient(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public void start() {
        stop();
        starterPool = Executors.newSingleThreadExecutor();
        starter = new Starter(serverInfo);
        starterPool.submit(starter);
//        new Thread(starter).start();
    }

    public void stop() {
        if (starter != null) {
            starter.close();
        }
        if (starterPool != null) {
            starterPool.shutdownNow();
        }
    }

    private class Starter implements Runnable {

        private final ServerInfo serverInfo;
        private volatile Socket socket;
        private volatile boolean done = false;
        private volatile ExecutorService handlerPool = null;
        private volatile MessageHandler messageHandler = null;

        public Starter(ServerInfo serverInfo) {
            this.serverInfo = serverInfo;
        }

        public void close() {
            done = true;
            if (messageHandler != null) {
                messageHandler.close();
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (handlerPool != null) {
                handlerPool.shutdownNow();
            }
            starterPool.shutdownNow();
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
            handlerPool = Executors.newSingleThreadExecutor();
            messageHandler = new MessageHandler(socket);
            handlerPool.submit(messageHandler);
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

        private class MessageHandler implements Runnable {

            private final Socket socket;
            private volatile boolean done = false;
            private volatile InputStream inputStream;

            public MessageHandler(Socket socket) {
                this.socket = socket;
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
                handlerPool.shutdownNow();

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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }


    public static void main(String[] args) throws InterruptedException {
        TCPClient client = new TCPClient(new ServerInfo("127.0.0.1", 4567));
        client.start();
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();

//        while (true) {
//            int numbers = threadGroup.activeCount();
//            Thread[] threads = new Thread[numbers];
//            threadGroup.enumerate(threads);
//            for (int i = 0; i < numbers; i++) {
//                System.out.println("线程号：" + i + " = " + threads[i].getName());
//            }
//            System.out.println();
//            Thread.sleep(1000);
//        }
    }

}
