package core;

import java.io.Closeable;
import java.io.IOException;

public class IOContext {

    private static IOContext INSTANCE;

    private final IOProvider ioProvider;

    private IOContext(IOProvider ioProvider) {
        this.ioProvider = ioProvider;
    }

    public IOProvider getIOProvider() {
        return this.ioProvider;
    }

    public static IOContext get() {
        return INSTANCE;
    }

    public static StartedBoot setup() {
        return new StartedBoot();
    }

    public static void close() {
        if (INSTANCE != null) {
            INSTANCE.callClose();
        }
    }

    private void callClose() {
        try {
            ioProvider.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class StartedBoot {
        private IOProvider ioProvider;

        public StartedBoot() {
        }

        public StartedBoot ioProvider(IOProvider ioProvider) {
            this.ioProvider = ioProvider;
            return this;
        }

        public IOContext start() {
            INSTANCE = new IOContext(ioProvider);
            return INSTANCE;
        }
    }

}
