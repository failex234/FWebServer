package me.felixnaumann.fwebserver;

import me.felixnaumann.fwebserver.server.Server;

public class FWebServer {

    private static boolean silenceflag = false;
    private static boolean running = false;

    public static void main(String[] args) {
        if (!running) {
            if (args.length == 1) {
                startServer(args);
            } else if (args.length > 1) {
                for (int i = 0; i < args.length - 1; i++) {
                    switch (args[i]) {
                        case "-s", "--silence" -> silenceflag = true;
                        default -> {
                            System.out.printf("unrecognized argument %s\n", args[i]);
                            usage();
                            System.exit(1);
                        }
                    }
                }
                startServer(args);
            } else {
                System.exit(1);
            }
        }
    }

    private static void startServer(String[] args) {
        if (!running) {
            running = true;
            try {
                int port = Integer.parseInt(args[args.length - 1]);
                Server.getInstance(port, silenceflag);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number");
                System.exit(0);
            }
        }
    }

    public static void usage() {
        System.out.println("usage: FWebServer <port>");
        System.out.println("OR");
        System.out.println("       FWebServer <args> <port>");
        System.out.println("\narguments:");
        System.out.println("-s --silence      - Suppress the output of log messages");
    }

    private FWebServer() {

    }
}
