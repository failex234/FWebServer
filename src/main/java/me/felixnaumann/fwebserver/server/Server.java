package me.felixnaumann.fwebserver.server;

import me.felixnaumann.fwebserver.model.ClientHeader;
import me.felixnaumann.fwebserver.model.ServerConfig;
import me.felixnaumann.fwebserver.utils.*;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.*;

public class Server {

    public static int port;
    public static String NAME = "FWebServer";
    public static final String VERSION = "0.3.1";
    private ServerSocket mainsocket;
    private Thread incoming;

    public static ArrayList<String> blacklist = new ArrayList<>();

    public static ServerConfig config;
    public static boolean configloaded;

    //TODO: Move all header related stuff to socket thread.
    public static ClientHeader currentHeader = null;

    public static HashMap<String, StringBuilder> scriptresults = new HashMap<>();
    public static HashMap<String, ClientHeader> scriptheader = new HashMap<>();

    public static String currdir;

    public static File wantedfile;
    public static String wantedfilemime;
    public static Date wantedfileLastModified;

    private static Server srv = null;

    public static Server getInstance(int port, boolean silenced) {
        if (srv == null) {
            srv = new Server(port, silenced);
        }
        return srv;
    }

    public static Server getInstance() {
        return srv;
    }


    private Server(int port, boolean silenced) {
        currdir = "";
        wantedfilemime = "text/html";

        prepareConfig();
        LogUtils.prepareLog();
        this.port = port;
        LogUtils.setSilenced(silenced);
        startServer();
    }

    /**
     * Prepares the server's config. Either by reading an existing config or creating a new one.
     */
    private void prepareConfig() {
        this.config = FileUtils.getServerConfig();

        this.NAME = config.getServername();

        FileUtils.createWwwRoot(config.getWwwroot(), config);
        configloaded = true;
    }

    /**
     * starts the main thread
     */
    private void startServer() {
        try {
            mainsocket = new ServerSocket(port);
            incoming = new Thread(new IncomingThread(mainsocket, port));
            incoming.start();
        } catch (BindException be) {
            System.err.printf("Cannot bind port :%d. Port may already be in use.\n", port);
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Tries to gracefully stop the server.
     */
    public void stopServer() {
        try {
            LogUtils.consolelog("Trying to stop server");
            mainsocket.close();
            incoming.interrupt();
        }
        catch (IOException ignored) {

        }
        System.exit(0);
    }


    public static ClientHeader getCurrentHeader() {
        return currentHeader;
    }


    public static void setCurrentHeader(ClientHeader header) {
        currentHeader = header;
    }
}
