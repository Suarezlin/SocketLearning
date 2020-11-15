package client;

import org.junit.Test;
import utils.ServerInfo;

import java.io.IOException;

public class TCPClientTest {

    @Test
    public void test() throws IOException {

        UDPSearcher searcher = new UDPSearcher(12345, 5000);
        ServerInfo serverInfo = searcher.search();
        if (searcher == null) {
            System.out.println("未搜索到服务器");
            return;
        }
        System.out.println(serverInfo);

        TCPClient client = new TCPClient(serverInfo);
        client.start();
//        for (int i = 0; i < 1; i++) {
//            TCPClient client = new TCPClient(serverInfo);
//            client.start();
//        }


    }

}