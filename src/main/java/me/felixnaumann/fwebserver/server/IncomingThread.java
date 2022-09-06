package me.felixnaumann.fwebserver.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class IncomingThread implements Runnable {

    private ServerSocket mainsocket;
    private int port;

    private static ArrayList<Thread> currentThreads = new ArrayList<>();

    public IncomingThread(ServerSocket socket, int port) {
        this.mainsocket = socket;
        this.port = port;
    }

    @Override
    public void run() {
        System.out.printf("%s listening on port %d\n", Server.getInstance().NAME, port);
        while (!Thread.currentThread().isInterrupted() && !mainsocket.isClosed()) {
            try {
                Socket socket = mainsocket.accept();
                Thread newThread = new Thread(new SocketThread(socket));
                currentThreads.add(newThread);
                newThread.start();
            } catch (IOException e) {

            }
        }
    }

    public static void interruptAllThreads() {
        for (Thread t : currentThreads) {
            t.interrupt();
        }
    }

    public static void completeThread(Thread t) {
        currentThreads.remove(t);
        t.interrupt();
    }
}