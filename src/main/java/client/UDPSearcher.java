package client;

import org.apache.commons.lang3.ArrayUtils;
import utils.ServerInfo;
import utils.SocketUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UDPSearcher {

    private final List<ServerInfo> servers = new ArrayList<>();

    private ExecutorService searcherPool;

    private final int port;

    private final long timeout;

    public UDPSearcher(int port, long timeout) {
        this.port = port;
        this.timeout = timeout;
    }

    public synchronized ServerInfo search() {
        stop();
        searcherPool = Executors.newSingleThreadExecutor();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completeLatch = new CountDownLatch(1);
        Searcher searcher = new Searcher(port, startLatch, completeLatch);
        searcherPool.submit(searcher);
//        new Thread(searcher).start();
        try {
            startLatch.await();
            completeLatch.await(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            stop();
            Thread.currentThread().interrupt();
        }
        searcher.close();
        searcherPool.shutdown();
        return stopAndGet();

    }

    private synchronized void stop() {
        if (searcherPool != null) {
            searcherPool.shutdown();
        }
        servers.clear();
    }

    public synchronized ServerInfo stopAndGet() {
        ServerInfo result = null;
        if (!servers.isEmpty()) {
            result = servers.get(0);
        } else {
            return null;
        }
        stop();
        return result;
    }


    private class Searcher implements Runnable {

        private final int port;
        private final CountDownLatch startLatch;
        private final CountDownLatch completeLatch;
        private volatile boolean done = false;
        private byte[] buffer = new byte[1024];
        private DatagramPacket receivePacket = new DatagramPacket(buffer, 0, buffer.length);
        private DatagramSocket socket;

        public Searcher(int port, CountDownLatch startLatch, CountDownLatch completeLatch) {
            this.port = port;
            this.startLatch = startLatch;
            this.completeLatch = completeLatch;
        }

        public void close() {
            done = true;
            if (socket != null) {
                socket.close();
            }
            searcherPool.shutdown();
        }

        @Override
        public void run() {
            try {
                socket = new DatagramSocket(port);
                System.out.println("UDP 监听端口: " + port);
                startLatch.countDown();
                DatagramPacket sendPacket = new DatagramPacket(ArrayUtils.addAll(SocketUtils.UDP_HEADER, SocketUtils.int2ByteArray(port)), 12, InetAddress.getByName("255.255.255.255"), 45678);
                socket.send(sendPacket);
                System.out.println("向 255.255.255.255 发送消息: " + new String(SocketUtils.UDP_HEADER) + port);
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
                        int tcpPort = SocketUtils.byteArray2Int(buffer, 8);
                        ServerInfo serverInfo = new ServerInfo(ip.substring(1), tcpPort);
                        servers.add(serverInfo);
                        completeLatch.countDown();
                    }
                }
            } catch (Exception ignore) {

            }
        }
    }

    public static void main(String[] args) {
        UDPSearcher searcher = new UDPSearcher(12345, 10000);
        ServerInfo serverInfo = searcher.search();
        System.out.println(serverInfo);
    }


}
