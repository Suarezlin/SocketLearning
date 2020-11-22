package core;

import core.impl.SocketChannelAdapter;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 用于服务端与客户端之间的交互，当 TCP 连接建立时，服务端与客户端都会构建一个连接用于之后的通信
 */
public class Connector implements Closeable, SocketChannelAdapter.OnChannelStatusChangedListener {
    // 用于标识一个连接
    private UUID key = UUID.randomUUID();
    // 连接的通道
    private SocketChannel channel;
    // 用于发送数据
    private Sender sender;
    // 用于接收数据
    private Receiver receiver;

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public void setup(SocketChannel socketChannel) throws IOException {
        this.channel = socketChannel;

        IOContext ioContext = IOContext.get();
        SocketChannelAdapter adapter = new SocketChannelAdapter(channel, ioContext.getIOProvider(), this);
        this.sender = adapter;
        this.receiver = adapter;
        readNextMessage();
    }

    private void readNextMessage() {
        if (receiver != null) {
            try {
                receiver.receiveAsync(echoReceiveListener);
            } catch (IOException e) {
                System.out.println("接收数据异常: " + e.getMessage());
            }
        }
    }

//    public void send(String msg) {
//        IOArgs args = new IOArgs();
//
//        sender.sendAsync()
//    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            sender.close();
            receiver.close();
            channel.close();
        }
    }

    @Override
    public void onChannelClosed(SocketChannel channel) {

    }

    private IOArgs.IOArgsEventListener echoReceiveListener = new IOArgs.IOArgsEventListener() {
        @Override
        public void onStated(IOArgs args) {

        }

        @Override
        public void onCompleted(IOArgs args) {
            onReceiveNewMessage(args.bufferString());
            readNextMessage();
        }
    };

    protected void onReceiveNewMessage(String str) {
        System.out.println(key.toString() + " : " + str);
    }
}
