package me.felixnaumann.fwebserver.server;

import me.felixnaumann.fwebserver.ClientHeader;
import me.felixnaumann.fwebserver.GenericServerRunnable;
import me.felixnaumann.fwebserver.ServerConfig;
import me.felixnaumann.fwebserver.utils.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.*;

//TODO: pyfs zu python: Einrückung fixen!
//TODO: add optional setting to inject html tags into the response
public class Server {

    int port;
    public static String SERVERNAME = "FWebServer";
    public static final String VERSION = "0.3.1";
    private ServerSocket mainsocket;
    private Thread incoming;

    public static ArrayList<String> blacklist = new ArrayList<>();
    private ArrayList<String> indexfiles = new ArrayList<>();

    public static HashMap<String, GenericServerRunnable> specialkeywords = new HashMap<>();
    public static HashMap<Integer, String> httpstatusCodes = new HashMap<>();

    public static ServerConfig config;

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


    private Server(int port, boolean silenced) {
        currdir = "";
        wantedfilemime = "text/html";
        httpstatusCodes.put(200, "OK");
        httpstatusCodes.put(201, "Created");
        httpstatusCodes.put(202, "Accepted");
        httpstatusCodes.put(204, "No Centent");
        httpstatusCodes.put(205, "Reset Content");
        httpstatusCodes.put(206, "Partial Content");
        httpstatusCodes.put(301, "Moved Permanently");
        httpstatusCodes.put(302, "Found (Moved Temporarily)");
        httpstatusCodes.put(304, "Not Modified");
        httpstatusCodes.put(400, "Bad Request");
        httpstatusCodes.put(401, "Unauthorized");
        httpstatusCodes.put(403, "Forbidden");
        httpstatusCodes.put(404, "Not Found");
        httpstatusCodes.put(411, "Length Required");
        httpstatusCodes.put(413, "Request Entity Too Large");
        httpstatusCodes.put(500, "Internal Server Error");
        httpstatusCodes.put(501, "Not Implemented");

        prepareConfig();
        LogUtils.prepareLog();
        prepareHTMLProcessor();
        this.port = port;
        LogUtils.setSilenced(silenced);
        start();
    }

    //TODO: uniform method for setting the whole respond header
    /**
     * Prepares the server's config. Either by reading an existing config or creating a new one.
     */
    private void prepareConfig() {
        this.config = FileUtils.getServerConfig();

        this.SERVERNAME = config.getServername();

        FileUtils.createWwwRoot(config.getWwwroot(), config);
    }

    /**
     * Add keywords with their functions to the hashmap
     * The HTML processor will later replace the keywords with the
     * result of each function.
     */
    private void prepareHTMLProcessor() {
        specialkeywords.put("useragent", ClientHeader::getUseragent);
        specialkeywords.put("serverversion", header -> VERSION);
        specialkeywords.put("servername", header -> SERVERNAME);
        specialkeywords.put("jreversion", header -> System.getProperty("java.version"));
        specialkeywords.put("osname", header -> System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        specialkeywords.put("username", header -> System.getProperty("user.name"));
        specialkeywords.put("accesslogpath", header -> config.getAccesslog().getAbsolutePath());
        specialkeywords.put("errorlogpath", header -> config.getErrorlog().getAbsolutePath());
        specialkeywords.put("configfilepath", header -> config.getConfigFile().getAbsolutePath());
        specialkeywords.put("wwwrootpath", header -> (new File(config.getWwwroot()).getAbsolutePath()));
        specialkeywords.put("today", header -> (new Date()).toString());
        specialkeywords.put("headertype", ClientHeader::getRequesttype);
        specialkeywords.put("headerversion", ClientHeader::getVersion);
        specialkeywords.put("headerhost", ClientHeader::getHost);
        specialkeywords.put("compiledate", new GenericServerRunnable() {
            @Override
            public String run(ClientHeader header) {
                try {
                    File jarFile = new File (this.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
                    return (new Date(jarFile.lastModified())).toString();
                }
                catch (URISyntaxException e) {
                    return "ERROR";
                }
            }
        });

        for (String key : config.getCustomkeywords().keySet()) {
            specialkeywords.put(key, header -> config.getCustomkeywords().get(key));
        }
        specialkeywords.put("keywordlist", header -> {
            StringBuilder endstring = new StringBuilder();

            for (String keys : specialkeywords.keySet()) {
                endstring.append(keys.replace("(", "&#40;").replace(")", "&#41;"));
                endstring.append("<br>");
            }
            return endstring.toString();
        });
    }

    /**
     * starts the main thread
     */
    private void start() {
        try {
            mainsocket = new ServerSocket(port);
            incoming = new Thread(new IncomingThread(mainsocket, port));
            incoming.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static ClientHeader getCurrentHeader() {
        return currentHeader;
    }


    public static void setCurrentHeader(ClientHeader header) {
        currentHeader = header;
    }


}
