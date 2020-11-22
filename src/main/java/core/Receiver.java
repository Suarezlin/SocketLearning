package core;

import java.io.Closeable;
import java.io.IOException;

public interface Receiver extends Closeable {

    boolean receiveAsync(IOArgs.IOArgsEventListener listener) throws IOException;

}
