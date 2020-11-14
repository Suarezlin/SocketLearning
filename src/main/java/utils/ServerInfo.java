package utils;

public class ServerInfo {

    private final String ip;

    private final int port;

    public ServerInfo(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                '}';
    }
}
