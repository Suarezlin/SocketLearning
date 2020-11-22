package core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * IO 输入输出工具类
 */
public class IOArgs {
    // 缓冲区
    private byte[] byteBuffer = new byte[1024];
    private ByteBuffer buffer = ByteBuffer.wrap(byteBuffer);

    // 读取
    public int read(SocketChannel channel) throws IOException {
        buffer.clear();
        return channel.read(buffer);
    }

    // 写入
    public int write(SocketChannel channel) throws IOException {
        return channel.write(buffer);
    }

    // 获取缓冲区内容
    public String bufferString() {
        return new String(byteBuffer, 0, buffer.position());
    }

    /**
     * IO 事件监听器
     */
    public interface IOArgsEventListener {
        // 操作开始回调
        void onStated(IOArgs args);
        // 操作完成回调
        void onCompleted(IOArgs args);
    }

}
