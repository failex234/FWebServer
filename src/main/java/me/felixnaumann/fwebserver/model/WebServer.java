package me.felixnaumann.fwebserver.model;

import me.felixnaumann.fwebserver.server.Server;

public abstract class WebServer {
    public final int PORT;
    public final String NAME;
    public final String VERSION = "0.3.1";

    protected WebServer(int port, String name) {
        this.PORT = port;
        this.NAME = name;
    }

    public int getPort() {
        return this.PORT;
    }

    public String getName() {
        return this.NAME;
    }

    public String getVersion() {
        return this.VERSION;
    }
}
