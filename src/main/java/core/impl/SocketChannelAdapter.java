package core.impl;

import core.IOArgs;
import core.IOProvider;
import core.Receiver;
import core.Sender;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketChannelAdapter implements Sender, Receiver, Closeable {

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private final SocketChannel channel;

    private final IOProvider ioProvider;

    private final OnChannelStatusChangedListener listener;

    private IOArgs.IOArgsEventListener receiveIOEventListener;

    private IOArgs.IOArgsEventListener sendIOEventListener;

    public SocketChannelAdapter(SocketChannel channel, IOProvider ioProvider, OnChannelStatusChangedListener listener) throws IOException {
        this.channel = channel;
        this.ioProvider = ioProvider;
        this.listener = listener;

        channel.configureBlocking(false);
    }

    @Override
    public boolean receiveAsync(IOArgs.IOArgsEventListener listener) throws IOException {

        if (isClosed.get()) {
            throw new IOException("Current Channel is closed");
        }

        receiveIOEventListener = listener;

        return ioProvider.registerInput(channel, inputCallback);
    }

    @Override
    public boolean sendAsync(IOArgs args, IOArgs.IOArgsEventListener listener) throws IOException {
        if (isClosed.get()) {
            throw new IOException("Current Channel is closed");
        }
        sendIOEventListener = listener;
        outputCallback.setAttach(args);
        return ioProvider.registerOutput(channel, outputCallback);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            ioProvider.unRegisterInput(channel);
            ioProvider.unRegisterInput(channel);
            channel.close();
            listener.onChannelClosed(channel);
        }
    }

    private final IOProvider.HandleInputCallback inputCallback = new IOProvider.HandleInputCallback() {

        @Override
        protected void provideInput() {
            if (isClosed.get()) {
                return;
            }

            IOArgs args = new IOArgs();
            IOArgs.IOArgsEventListener receiveIOEventListener = SocketChannelAdapter.this.receiveIOEventListener;

            if (receiveIOEventListener != null) {
                receiveIOEventListener.onStated(args);
            }

            // 读取
            try {
                if (receiveIOEventListener != null && args.read(channel) > 0) {
                    receiveIOEventListener.onCompleted(args);
                } else {
                    throw new IOException("Cannot read any data");
                }
            } catch (IOException ignored) {
                try {
                    SocketChannelAdapter.this.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private final IOProvider.HandleOutputCallback outputCallback = new IOProvider.HandleOutputCallback() {
        @Override
        protected void provideOutput(Object attach) {
            if (isClosed.get()) {
                return;
            }
            //TODO
            sendIOEventListener.onCompleted(null);
        }
    };

    public interface OnChannelStatusChangedListener {
        void onChannelClosed(SocketChannel channel);
    }
}
