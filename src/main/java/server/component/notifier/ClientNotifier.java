package server.component.notifier;

import server.component.handler.ClientHandler;

public interface ClientNotifier {

    void notifyAdd(ClientHandler clientHandler);

    void notifyRemove(ClientHandler clientHandler);

    void notifyBroadcast(String msg);

}
