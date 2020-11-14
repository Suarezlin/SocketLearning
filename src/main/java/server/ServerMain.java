package server;

import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) throws IOException {
        TCPServer.start(4567);
        UDPProvider.start(45678, 4567);

        System.in.read();
        TCPServer.stop();
        UDPProvider.stop();
    }

}
