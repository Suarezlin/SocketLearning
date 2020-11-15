package client;

import client.component.handler.Starter;
import utils.ServerInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClientTest {

    public static void main(String[] args) throws IOException, InterruptedException {
        UDPSearcher searcher = new UDPSearcher(12345, 5000);
        ServerInfo serverInfo = searcher.search();
        if (searcher == null) {
            System.out.println("未搜索到服务器");
            return;
        }
        System.out.println(serverInfo);
        List<TCPClient> clients = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            TCPClient client = new TCPClient(serverInfo);
            client.start();
            clients.add(client);

        }
        Thread.sleep(1000);
        for (int i = 0; i < 1000; i++) {
            clients.get(i).send("启动第 " + (i + 1) + "个线程");
        }

        System.out.println("jkljlk");

    }

}
