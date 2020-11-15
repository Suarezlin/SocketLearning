package client;

import client.component.handler.Starter;
import client.component.notifier.CloseNotifier;
import utils.ServerInfo;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class TCPClient implements CloseNotifier {

    private volatile Starter starter;
    private final ServerInfo serverInfo;
    private volatile ExecutorService starterPool;

    public TCPClient(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public synchronized void start() {
        stop();
        starterPool = Executors.newSingleThreadExecutor();
        starter = new Starter(this, serverInfo);
        starterPool.submit(starter);
    }

    public synchronized void stop() {
        if (starter != null) {
            starter.close();
        }
        notifyClose();
    }

    public synchronized void send(String msg) {
        if (starter != null) {
            starter.send(msg);
        }
    }

    @Override
    public void notifyClose() {
        if (starterPool != null) {
            starterPool.shutdown();
        }
    }


    public static void main(String[] args) throws InterruptedException {
        TCPClient client = new TCPClient(new ServerInfo("127.0.0.1", 4567));
        client.start();

//        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
//
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
