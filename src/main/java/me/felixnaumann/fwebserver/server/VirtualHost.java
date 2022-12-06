package me.felixnaumann.fwebserver.server;

import me.felixnaumann.fwebserver.model.WebServer;
import me.felixnaumann.fwebserver.utils.*;

import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.util.*;

public class VirtualHost extends WebServer implements Runnable {
    private ServerSocket mainsocket;
    private Thread incoming;

    public ArrayList<String> blacklist = new ArrayList<>();

    public static String currdir;

    public VirtualHost(int port, String wwwroot, String[] indexfiles, boolean noindex, boolean silenced) {
        super(port, "FWebServer", wwwroot, indexfiles, noindex);
        currdir = "";

        prepareWwwRoot();
        LogUtils.prepareLog();
        LogUtils.setSilenced(silenced);
    }

    /**
     * Prepares the server's config. Either by reading an existing config or creating a new one.
     */
    private void prepareWwwRoot() {
        FileUtils.createWwwRoot(getWwwRoot());
    }

    /**
     * starts the main thread
     */
    private void startServer() {
        try {
            mainsocket = new ServerSocket(this.getPort());
            incoming = new Thread(new IncomingThread(mainsocket, this, this.getPort()));
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

    @Override
    public void run() {
        startServer();
    }
}
