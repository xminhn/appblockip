package com.example.blockweb;

public class Device {
    private String mac;
    private String ip;
    private String hostname;
    private boolean blocked;

    public Device(String mac, String ip, String hostname, boolean blocked) {
        this.mac = mac;
        this.ip = ip;
        this.hostname = hostname;
        this.blocked = blocked;
    }

    public String getMac() {
        return mac;
    }

    public String getIp() {
        return ip;
    }

    public String getHostname() {
        return hostname;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}