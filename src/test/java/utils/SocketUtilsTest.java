package utils;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.*;
import java.net.Socket;

@RunWith(MockitoJUnitRunner.class)
public class SocketUtilsTest extends TestCase {

    @Mock
    private Socket socket;



    @Test
    public void testSend() throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Mockito.when(socket.getOutputStream()).thenReturn(out);
        boolean status = SocketUtils.send(socket, "test");
        assertTrue(status);
        byte[] result = out.toByteArray();
        for (byte b : result) {
            System.out.printf("%c ", b);
        }
        System.out.println();
    }

    @Test
    public void testInt2ByteArray() {

        int length = 158;

        byte[] array = SocketUtils.int2ByteArray(158);
        assertEquals((byte) 0, array[0]);
        assertEquals((byte) 0, array[1]);
        assertEquals((byte) 0, array[2]);
        assertEquals((byte) 0x9E, array[3]);
    }

    @Test
    public void testByteArray2Int() {

        byte[] data = new byte[] {0, 0, 0, (byte) 0x9E};
        int integer = SocketUtils.byteArray2Int(data);
        assertEquals(158, integer);
    }

    @Test
    public void testReceive() throws IOException {

        byte[] data = new byte[] {
              (byte) 't', (byte) 'e', (byte) 's', (byte) 't'
        };

        final ByteArrayInputStream in = new ByteArrayInputStream(data);
        Mockito.when(socket.getInputStream()).thenReturn(in);
        SocketUtils.ReceiveMsg msg = SocketUtils.receive(socket);
        assertTrue(msg.isStatus());
        assertEquals("test", msg.getMsg());
        System.out.println(msg);

    }
}