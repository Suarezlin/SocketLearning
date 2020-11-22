package core.impl;

import core.IOProvider;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class IOSelectorProvider implements IOProvider {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private final AtomicBoolean inRegInput = new AtomicBoolean(false);

    private final AtomicBoolean inRegOutput = new AtomicBoolean(false);

    private final Selector readSelector;

    private final Selector writeSelector;

    private final Map<SelectionKey, Runnable> inputCallbackMap = new HashMap<>();

    private final Map<SelectionKey, Runnable> outputCallbackMap = new HashMap<>();

    private final ExecutorService inputHandlerPool;

    private final ExecutorService outputHandlerPool;

    public IOSelectorProvider() throws IOException {
        readSelector = Selector.open();
        writeSelector = Selector.open();
        inputHandlerPool = Executors.newFixedThreadPool(4, new IOProviderThreadFactory("Input-Handler-Thread"));
        outputHandlerPool = Executors.newFixedThreadPool(4, new IOProviderThreadFactory("Output-Handler-Thread"));
        startRead();
        startWrite();
    }

    private void startRead() {
        Thread thread = new Thread("IOSelectorProvider Read Selector Thread") {
            @Override
            public void run() {
                while (!isClosed.get()) {
                    try {
                        if (readSelector.select() == 0) {
                            waitSelection(inRegInput);
                            continue;
                        }

                        Set<SelectionKey> selectionKeys = readSelector.selectedKeys();
                        for (SelectionKey key : selectionKeys) {
                            if (key.isValid()) {
                                handleSelection(key, SelectionKey.OP_READ, inputCallbackMap, inputHandlerPool);
                            }
                        }
                        selectionKeys.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private void startWrite() {
        Thread thread = new Thread("IOSelectorProvider Write Selector Thread") {
            @Override
            public void run() {
                while (!isClosed.get()) {
                    try {
                        if (writeSelector.select() == 0) {
                            waitSelection(inRegOutput);
                            continue;
                        }

                        Set<SelectionKey> selectionKeys = writeSelector.selectedKeys();
                        for (SelectionKey key : selectionKeys) {
                            if (key.isValid()) {
                                handleSelection(key, SelectionKey.OP_WRITE, outputCallbackMap, outputHandlerPool);
                            }
                        }
                        selectionKeys.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    @Override
    public boolean registerInput(SocketChannel channel, HandleInputCallback callback) {

        SelectionKey key = registerSelection(channel, readSelector, SelectionKey.OP_READ, inRegInput, inputCallbackMap, callback);

        return key != null;
    }

    @Override
    public boolean registerOutput(SocketChannel channel, HandleOutputCallback callback) {
        SelectionKey key = registerSelection(channel, writeSelector, SelectionKey.OP_WRITE, inRegOutput, outputCallbackMap, callback);

        return key != null;
    }

    @Override
    public void unRegisterInput(SocketChannel channel) {
        unRegisterSelection(channel, readSelector, inputCallbackMap);
    }

    @Override
    public void unRegisterOutput(SocketChannel channel) {
        unRegisterSelection(channel, writeSelector, outputCallbackMap);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            inputHandlerPool.shutdown();
            outputHandlerPool.shutdown();
            inputCallbackMap.clear();
            outputCallbackMap.clear();
            readSelector.wakeup();
            writeSelector.wakeup();

            readSelector.close();
            writeSelector.close();
        }
    }

    private static void handleSelection(SelectionKey key, int keyOps, Map<SelectionKey, Runnable> inputCallbackMap, ExecutorService inputHandlerPool) {
        // 取消注册，防止被再次处理
        key.interestOps(key.readyOps() & ~keyOps);

        Runnable runnable = inputCallbackMap.get(key);
        if (runnable != null && !inputHandlerPool.isShutdown()) {
            inputHandlerPool.execute(runnable);
        }
    }

    private static void waitSelection(final AtomicBoolean locker) {
        synchronized (locker) {
            if (locker.get()) {
                try {
                    locker.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static SelectionKey registerSelection(SocketChannel channel, Selector selector, int registerOps, AtomicBoolean locker, Map<SelectionKey, Runnable> map, Runnable runnable) {
        synchronized (locker) {
            locker.set(true);
            try {
                // 取消 select 状态
                selector.wakeup();
                SelectionKey key = null;
                if (channel.isRegistered()) {
                    key = channel.keyFor(selector);
                    if (key != null) {
                        key.interestOps(key.readyOps() | registerOps);
                    }
                }
                if (key == null) {
                    key = channel.register(selector, registerOps);
                    map.put(key, runnable);
                }
                return key;
            } catch (ClosedChannelException e) {
                return null;
            } finally {
                locker.set(false);
                try {
                    locker.notify();
                } catch (Exception ignored) {};

            }
        }
    }

    private static void unRegisterSelection(SocketChannel channel, Selector selector, Map<SelectionKey, Runnable> map) {
        if (channel.isRegistered()) {
            SelectionKey key = channel.keyFor(selector);
            if (key != null) {
                // 取消监听
                key.cancel();
                map.remove(key);
                selector.wakeup();
            }
        }
    }

    static class IOProviderThreadFactory implements ThreadFactory {

        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String prefix;

        public IOProviderThreadFactory(String prefix) {
            this.prefix = prefix;
            SecurityManager s = System.getSecurityManager();
            this.group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, prefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
