package me.felixnaumann.fwebserver.server;

import me.felixnaumann.fwebserver.model.Request;
import me.felixnaumann.fwebserver.model.RequestHeader;
import me.felixnaumann.fwebserver.model.ServerConfig;
import me.felixnaumann.fwebserver.model.WebServer;
import me.felixnaumann.fwebserver.utils.*;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.*;

public class Server extends WebServer {
    private ServerSocket mainsocket;
    private Thread incoming;

    public static ArrayList<String> blacklist = new ArrayList<>();
    public ArrayList<Request> liverequests = new ArrayList<>();

    public static ServerConfig config;
    public static boolean configloaded;

    public HashMap<String, StringBuilder> scriptresults = new HashMap<>();
    public HashMap<String, RequestHeader> scriptheader = new HashMap<>();

    public static String currdir;

    private static Server serverinstance = null;

    public static Server getInstance(int port, boolean silenced) {
        if (serverinstance == null) {
            serverinstance = new Server(port, silenced);
        }
        return serverinstance;
    }

    public static Server getInstance() {
        return getInstance(3000, false);
    }

    private Server(int port, boolean silenced) {
        super(port, "FWebServer");
        currdir = "";

        prepareConfig();
        LogUtils.prepareLog();
        LogUtils.setSilenced(silenced);
        startServer();
    }

    /**
     * Prepares the server's config. Either by reading an existing config or creating a new one.
     */
    private void prepareConfig() {
        this.config = FileUtils.getServerConfig();

        FileUtils.createWwwRoot(config.getWwwroot(), config);
        configloaded = true;
    }

    /**
     * starts the main thread
     */
    private void startServer() {
        try {
            mainsocket = new ServerSocket(this.getPort());
            incoming = new Thread(new IncomingThread(mainsocket, this.getPort()));
            incoming.start();
        } catch (BindException be) {
            System.err.printf("Cannot bind port :%d. Port may already be in use.\n", this.getPort());
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
            IncomingThread.interruptAllThreads();

            Thread.sleep(1000);
            incoming.interrupt();
        }
        catch (IOException | InterruptedException ignored) {

        }
        System.exit(0);
    }
}
