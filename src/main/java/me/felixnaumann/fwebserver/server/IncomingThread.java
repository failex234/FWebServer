package me.felixnaumann.fwebserver.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class IncomingThread implements Runnable {

    private ServerSocket mainsocket;
    private int port;

    public IncomingThread(ServerSocket socket, int port) {
        this.mainsocket = socket;
        this.port = port;
    }

    @Override
    public void run() {
        System.out.printf("%s listening on port %d\n", Server.SERVERNAME, port);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Socket socket = mainsocket.accept();
                new Thread(new SocketThread(socket)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}