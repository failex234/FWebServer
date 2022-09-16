package me.felixnaumann.fwebserver.model;

public abstract class WebServer {
    private final int PORT;
    private final String NAME;
    private final String WWWROOT;
    private final String[] INDEXFILES;
    private final boolean NOINDEX;


    protected WebServer(int port, String name, String wwwroot, String[] indexfiles, boolean noindex) {
        this.PORT = port;
        this.NAME = name;
        this.WWWROOT = wwwroot;
        this.INDEXFILES = indexfiles;
        this.NOINDEX = noindex;
    }

    public int getPort() {
        return this.PORT;
    }

    public String getName() {
        return this.NAME;
    }


    public String getWwwRoot() {
        return this.WWWROOT;
    }

    public String[] getIndexFiles() {
        return this.INDEXFILES;
    }

    public boolean isNoIndex() {
        return NOINDEX;
    }

}
