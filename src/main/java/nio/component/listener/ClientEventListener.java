package nio.component.listener;

import nio.component.handler.ClientHandler;

public interface ClientEventListener {

    void onClientClose(ClientHandler clientHandler);

    void onClientCreate(ClientHandler clientHandler);
}
