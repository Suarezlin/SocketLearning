package core;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

/**
 * 用于注册通道的输入输出
 */
public interface IOProvider extends Closeable {

    boolean registerInput(SocketChannel channel, HandleInputCallback callback);

    boolean registerOutput(SocketChannel channel, HandleOutputCallback callback);

    void unRegisterInput(SocketChannel channel);

    void unRegisterOutput(SocketChannel channel);

    // 输入注册回调
    abstract  class HandleInputCallback implements Runnable {
        @Override
        public final void run() {
            provideInput();
        }

        protected abstract void provideInput();
    }

    // 输出注册回调
    abstract class HandleOutputCallback implements Runnable {

        private Object attach;

        @Override
        public final void run() {
            provideOutput(attach);
        }

        public final void setAttach(Object attach) {
            this.attach = attach;
        }

        protected abstract void provideOutput(Object attach);

    }

}
