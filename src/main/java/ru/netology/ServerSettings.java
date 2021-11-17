package ru.netology;

public class ServerSettings {
    private final String host;
    private final int port;

    public ServerSettings(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
