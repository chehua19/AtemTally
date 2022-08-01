package tech.yakov.AtemProxy.models;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class Client {
    private InetAddress ip;
    private int port;
    private long lastSendTime;
    public Client(InetAddress ip, int port) {
        this.ip = ip;
        this.port = port;
        lastSendTime = 0L;
    }

    public Client(DatagramPacket datagramPacket) {
        this.ip = datagramPacket.getAddress();
        this.port = datagramPacket.getPort();
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getLastSendTime() {
        return lastSendTime;
    }

    public void setLastSendTime(long lastSendTime) {
        this.lastSendTime = lastSendTime;
    }
}
