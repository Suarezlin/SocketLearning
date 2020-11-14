package server;

import org.apache.commons.lang3.ArrayUtils;
import utils.SocketUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPProvider {

    private static Provider provider;

    private static ExecutorService providerPool;

    public synchronized static void start(int port, int tcpPort) {
        stop();
        providerPool = Executors.newSingleThreadExecutor();
        provider = new Provider(port, tcpPort);
//        new Thread(provider).start();
        providerPool.submit(provider);
//        providerPool.shutdown();
    }

    public synchronized static void stop() {
        if (provider != null) {
            provider.close();
        }
        if (providerPool != null) {
            providerPool.shutdownNow();
        }
    }

    private static class Provider implements Runnable {

        private final int port;
        private final int tcpPort;
        private DatagramSocket socket;
        private final byte[] buffer = new byte[1024];
        private final DatagramPacket receivePacket = new DatagramPacket(buffer, 0, 1024);
        private volatile boolean done = false;

        private Provider(int port, int tcpPort) {
            this.port = port;
            this.tcpPort = tcpPort;
        }

        public void close() {
            done = true;
            if (socket != null) {
                socket.close();
            }
            providerPool.shutdownNow();
        }


        @Override
        public void run() {
            try {
                socket = new DatagramSocket(port);
            } catch (Exception e) {
                e.printStackTrace();
                close();
                return;
            }
            try {
                System.out.println("UDP 服务器已启动，端口: " + port);
                while (!done) {
                    socket.receive(receivePacket);
                    String ip = receivePacket.getAddress().toString();
                    int port = receivePacket.getPort();
                    int length = receivePacket.getLength();
                    byte[] buffer = receivePacket.getData();
                    System.out.println("收到来自 " + ip + ":" + port + ": ");
                    for (int i = 0; i < length; i++) {
                        System.out.printf("%x ", buffer[i]);
                    }
                    System.out.println();
                    if (length < 12 || !"cafebabe".equals(new String(buffer, 0, 8, StandardCharsets.UTF_8)) || SocketUtils.byteArray2Int(buffer, 8) <= 0) {
                        System.out.println("消息无效");
                    } else {
                        int responsePort = SocketUtils.byteArray2Int(buffer, 8);
                        // 回送消息
                        DatagramPacket sendPacket = new DatagramPacket(ArrayUtils.addAll(SocketUtils.UDP_HEADER, SocketUtils.int2ByteArray(tcpPort)), 12, receivePacket.getAddress(), responsePort);
                        socket.send(sendPacket);
                        System.out.println("向 "  + ip + ":" + responsePort + " 发送: " + new String(SocketUtils.UDP_HEADER) + tcpPort);
                    }
                }
            } catch (Exception ignore) {
            }

        }
    }

}
