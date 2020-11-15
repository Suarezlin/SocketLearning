package server.component;

import server.component.handler.Provider;
import server.component.notifier.CloseNotifier;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UDPProvider implements CloseNotifier {

    private volatile Provider provider;

    private volatile ExecutorService providerPool;

    private final int port;

    private final int tcpPort;

    public UDPProvider(int port, int tcpPort) {
        this.port = port;
        this.tcpPort = tcpPort;
    }

    public synchronized void start() {
        stop();
        providerPool = Executors.newSingleThreadExecutor();
        provider = new Provider(this, port, tcpPort);
        providerPool.submit(provider);
    }

    public synchronized void stop() {
        if (provider != null) {
            provider.close();
        }
        notifyClose();
    }

    @Override
    public void notifyClose() {
        if (providerPool != null) {
            providerPool.shutdown();
        }
    }
}
