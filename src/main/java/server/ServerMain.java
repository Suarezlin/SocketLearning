package server;

import server.component.TCPServer;
import server.component.UDPProvider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerMain {

    public static void main(String[] args) throws IOException, InterruptedException {

        TCPServer tcpServer = new TCPServer(4567);
        tcpServer.start();
        UDPProvider udpProvider = new UDPProvider(45678, 4567);
        udpProvider.start();

        Thread.sleep(10000);
        tcpServer.broadcast("test");


        while (!"quit".equals(new BufferedReader(new InputStreamReader(System.in)).readLine()));
        System.out.println("退出中...");
        tcpServer.stop();
        udpProvider.stop();

    }

}
