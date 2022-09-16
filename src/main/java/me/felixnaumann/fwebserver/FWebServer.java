package me.felixnaumann.fwebserver;

import me.felixnaumann.fwebserver.model.Config;
import me.felixnaumann.fwebserver.model.MainConfig;
import me.felixnaumann.fwebserver.model.RequestHeader;
import me.felixnaumann.fwebserver.server.VirtualHost;
import me.felixnaumann.fwebserver.utils.ConfigUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class FWebServer {

    private static boolean silenceflag = false;
    private static boolean running = false;
    private static Config config;
    private static ArrayList<Thread> threads = new ArrayList<>();

    public static MainConfig mainConfig;
    public static String VERSION = "0.4";

    public static HashMap<String, StringBuilder> scriptresults = new HashMap<>();
    public static HashMap<String, RequestHeader> scriptheader = new HashMap<>();

    public static void main(String[] args) {
        if (!running) {
            if (args.length > 0) {
                for (int i = 0; i < args.length - 1; i++) {
                    if (args[i].equals("-s") || args[i].equals("--silence")) {
                        silenceflag = true;
                    } else if (args[i].startsWith("-")) {
                        System.out.printf("unrecognized argument %s\n", args[i]);
                        usage();
                        System.exit(1);
                    }
                }
            }
            startServer();
        }
    }

    private static void startServer() {
        if (!running) {
            running = true;
            loadConfig();
            for (String host : config.getHosts()) {
                int port;
                String wwwroot;
                String[] indexfiles;

                port = config.getInt(String.format("Host.%s.port", host));
                wwwroot = config.getString(String.format("Host.%s.wwwroot", host));
                indexfiles = config.getStringArr(String.format("Host.%s.indexfiles", host));

                boolean noindex = config.getBool(String.format("Host.%s.noindex", host));

                if (port == 0) {
                    System.err.printf("Invalid port number for host %s\n", host);
                    System.exit(1);
                } else if (wwwroot == null || wwwroot.isBlank()) {
                    System.err.printf("Invalid wwwroot path for host %s\n", host);
                    System.exit(1);
                } else if (indexfiles == null || indexfiles.length == 0) {
                    System.err.printf("Invalid indexfiles for host %s\n", host);
                    System.exit(1);
                }

                Thread t = new Thread(new VirtualHost(port, wwwroot, indexfiles, noindex, silenceflag));
                t.start();

                threads.add(t);
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

    private static void loadConfig() {
        config = ConfigUtils.loadConfig();
        mainConfig = new MainConfig();
        mainConfig.createNewConfig();

        String servname = config.getString("Main.servername");
        String logsdir = config.getString("Main.logs_dir");
        String acclog = config.getString("Main.log_access_name");
        String errlog = config.getString("Main.log_error_name");
        boolean supress = config.getBool("Main.supress_version");

        mainConfig.setServername(servname);
        mainConfig.setLogfolder(logsdir);
        mainConfig.setAccesslog(acclog);
        mainConfig.setErrorlog(errlog);
        mainConfig.setSuppressversion(supress);

        //TODO load keywords
    }

}
