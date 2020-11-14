package utils;

import org.apache.commons.lang3.ArrayUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SocketUtils {

    public static final byte[] UDP_HEADER = new byte[] {
            (byte) 'c', (byte) 'a', (byte) 'f', (byte) 'e', (byte) 'b', (byte) 'a', (byte) 'b', (byte) 'e'
    };

    public static class ReceiveMsg {
        private final boolean status;
        private final String msg;

        private ReceiveMsg(boolean status, String msg) {
            this.status = status;
            this.msg = msg;
        }

        public boolean isStatus() {
            return status;
        }

        public String getMsg() {
            return msg;
        }

        @Override
        public String toString() {
            return "ReceiveMsg{" +
                    "status=" + status +
                    ", msg='" + msg + '\'' +
                    '}';
        }

        public static ReceiveMsg success(String msg) {
            return new ReceiveMsg(true, msg);
        }

        public static ReceiveMsg error(Exception e) {
            return new ReceiveMsg(false, e.getMessage());
        }
    }

    public static boolean send(Socket socket, String msg) {
        try {
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
//            byte[] length = int2ByteArray(msg.length());

//            out.write(ArrayUtils.addAll(length, msg.getBytes(StandardCharsets.UTF_8)));
            out.write(msg.getBytes(StandardCharsets.UTF_8));
            out.flush();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static ReceiveMsg receive(Socket socket) {
        try {
            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
//            byte[] lengthBytes = new byte[4];
//            int length;
//            if (in.read(lengthBytes, 0, 4) != -1) {
//                length = byteArray2Int(lengthBytes);
//            } else {
//                return ReceiveMsg.error();
//            }
            byte[] data = new byte[1024];
//            if (in.read(data, 0, length) != -1) {
//                return ReceiveMsg.success(new String(data, 0, length, StandardCharsets.UTF_8));
//            } else {
//                return ReceiveMsg.error();
//            }
            int length;
            StringBuilder buf = new StringBuilder();
            if ((length = in.read(data)) > 0) {
                buf.append(new String(data, 0, length, StandardCharsets.UTF_8));
                return ReceiveMsg.success(buf.toString());
            } else {
                return null;
            }

        } catch (IOException e) {
            return ReceiveMsg.error(e);
        }
    }



    public static byte[] int2ByteArray(int data) {
        byte[] res = new byte[4];
        res[0] = (byte) ((data >>> 24) & 0xFF);
        res[1] = (byte) ((data >>> 16) & 0xFF);
        res[2] = (byte) ((data >>> 8) & 0xFF);
        res[3] = (byte) (data & 0xFF);
        return res;
    }

    public static int byteArray2Int(byte[] data) {
        return byteArray2Int(data, 0);
    }

    public static int byteArray2Int(byte[] data, int index) {
        if (data.length < 4) {
            throw new IllegalArgumentException("Converting Integer needs 4 bytes");
        }
        return ((data[index++] & 0xFF) << 24) | ((data[index++] & 0xFF) << 16) | ((data[index++] & 0xFF) << 8) | (data[index] & 0xFF);
    }



}
