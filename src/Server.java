import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Server {

    int port;
    private String SERVERNAME = "FWebServer";
    private final String VERSION = "0.2.3";
    private ServerSocket mainsocket;
    private Thread incoming;
    private ArrayList<String> blacklist = new ArrayList<>();
    private HashMap<String, GenericServerRunnable> specialkeywords = new HashMap<>();
    private File accesslog;
    private File errorlog;
    private File configfile;
    private Gson gsoninstance;
    private ServerConfig config;

    String wwwroot;


    Server(int port) {
        prepareConfig();
        prepareLog();
        prepareHTMLProcessor();
        this.port = port;
        start();
    }

    /**
     * Reads a existing config file or creates a new one
     */
    private void prepareConfig() {
        StringBuilder json = new StringBuilder();
        gsoninstance = new GsonBuilder().setPrettyPrinting().create();
        configfile = new File("server.json");

        if (configfile.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(configfile));
                String line;
                while((line = br.readLine()) != null) {
                    json.append(line);
                }
                br.close();
                config = gsoninstance.fromJson(json.toString(), ServerConfig.class);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            config = new ServerConfig();
            config.createNewConfig();
            String jsonout = gsoninstance.toJson(config);

            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(configfile));
                bw.write(jsonout);
                bw.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }

        this.SERVERNAME = config.getServername();
        this.wwwroot = config.getWwwroot();

    }

    /**
     * Add keywords with their functions to the hashmap
     * The HTML processor will later replace the keywords with the
     * result of each function.
     */
    private void prepareHTMLProcessor() {
        specialkeywords.put("$(useragent)", this::getUserAgent);
        specialkeywords.put("$(serverversion)", header -> VERSION);
        specialkeywords.put("$(servername)", header -> SERVERNAME);
        specialkeywords.put("$(jreversion)", header -> System.getProperty("java.version"));
        specialkeywords.put("$(osname)", header -> System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        specialkeywords.put("$(username)", header -> System.getProperty("user.name"));
        specialkeywords.put("$(accesslogpath)", header -> accesslog.getAbsolutePath());
        specialkeywords.put("$(errorlogpath)", header -> errorlog.getAbsolutePath());
        specialkeywords.put("$(configfilepath)", header -> configfile.getAbsolutePath());
        specialkeywords.put("$(wwwrootpath)", header -> (new File(wwwroot).getAbsolutePath()));
        specialkeywords.put("$(compiledate)", new GenericServerRunnable() {
            @Override
            public String run(ArrayList<String> header) {
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
        specialkeywords.put("$(keywordlist)", header -> {
            StringBuilder endstring = new StringBuilder();

            for (String keys : specialkeywords.keySet()) {
                endstring.append(keys.replace("(", "&#40;").replace(")", "&#41;"));
                endstring.append("<br>");
            }
            return endstring.toString();
        });
    }

    /**
     * Create new log files if non exist
     */
    private void prepareLog() {
        accesslog = new File(config.getAccesslog());
        errorlog = new File(config.getErrorlog());
        File logfolder = new File(config.getLogfolder());

        if (!logfolder.exists()) {
            logfolder.mkdir();
        }
        try {
            accesslog.createNewFile();
            errorlog.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * starts the main thread
     */
    private void start() {
        try {
            mainsocket = new ServerSocket(port);
            incoming = new Thread(new IncomingThread());
            incoming.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * adds a string to the access log
     * @param tolog the string to append
     */
    private void logAccess(String tolog) {
        try {
            FileWriter fw = new FileWriter(accesslog, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);

            out.println(tolog);
            out.close();
            bw.close();
            fw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * adds a string to the error log
     * @param toerrorlog the string to append
     */
    private void logError(String toerrorlog) {
        try {
            FileWriter fw = new FileWriter(errorlog, true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);

            out.println(toerrorlog);
            out.close();
            bw.close();
            fw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the user agent of a browser from the header
     * @param header the received header
     * @return the useragent
     */
    private String getUserAgent(ArrayList<String> header) {
        String uaheader = "";
        for (String s : header) {
            if (s.startsWith("User-Agent:")) {
                uaheader = s;
            }
        }
        String[] splitted = uaheader.split(" ");
        StringBuilder uagent = new StringBuilder();

        for (String s : splitted) {
            if (s.equals("User-Agent:")) continue;
            uagent.append(s + " ");
        }

        return uagent.toString();
    }

    /**
     * 0 = not exist, 1 = exists, 2 = no permissions
     *
     * checks if a wanted file exists
     * @param filename the file to check
     * @return status code of the file
     */
    private int fileExists(String filename) {
        if (filename.startsWith("..") || filename.startsWith("/")) {
            filename = filename.replace("..", "").replaceFirst("/", "");
        }

        File temp = new File(wwwroot + "/" + filename);

        if (!blacklist.contains(filename)) {
            if (temp.exists()) {
                return 1;
            }
            return 0;
        }
        return 2;
    }

    /**
     * The HTML Processor replaces special keywords with results of functions
     * @param rawhtml the html from the file
     * @param header the client header
     * @return the processed HTML
     */
    private String processHTML(String rawhtml, ArrayList<String> header) {
        for(String keys : specialkeywords.keySet()) {
            rawhtml = rawhtml.replace(keys, specialkeywords.get(keys).run(header));
        }
        return rawhtml;
    }

    /**
     * Read a file in text mode (CURRENTLY NOT USABLE FOR BINARY FILES)
     * @param filename the file to read
     * @return the file contents
     */
    private String readFile(String filename) {
        //TODO: Read binary on non plain-text file
        if (filename.startsWith("..") || filename.startsWith("/")) {
            filename = filename.replaceFirst("/", "").replace("..", "");
        }

        File toberead = new File(wwwroot + "/" + filename);
        StringBuilder tempstring = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(toberead));
            String line;

            while((line = br.readLine()) != null) {
                tempstring.append(line);
                tempstring.append("\n");
            }
            return tempstring.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "<center><h1>FILE IO ERROR</center></h1>";
        }

    }

    public class IncomingThread implements Runnable {

        @Override
        public void run() {
            System.out.printf("%s listening on port %d\n", SERVERNAME, port);
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

    public class SocketThread implements Runnable {

        Socket socket;

        SocketThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                while (!socket.isClosed()) {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                        ArrayList<String> req = new ArrayList<>();

                        String line;
                        while ((line = br.readLine()) != null) {
                            req.add(line);
                            if (line.isEmpty()) {
                                break;
                            }
                        }
                        String[] allargs = req.get(0).split(" ");

                        if(allargs.length != 3) {
                            bw.write("HTTP/1.1 400 Bad Request\r\n");
                            bw.write("Server: " + SERVERNAME + "\r\n");
                            bw.write("");
                            bw.write("\r\n");
                            bw.write("<!doctype html>\n<html>\n<body>");
                            bw.write("<center><h1>400 Bad Request</h1></center>");
                            bw.write("<center><h3>Invalid request header!/center></h3>");
                            bw.write("</body>");
                            bw.write("</html>");
                        }

                        switch (allargs[0]) {
                            case "GET":
                                System.out.printf("[%s] GET %s\n", socket.getInetAddress().toString(), allargs[1]);
                                logAccess("[" + socket.getInetAddress().toString() + "] GET " + allargs[1]);
                                if (allargs[1].equals("/")) {
                                    //TODO: Find index.html / index.htm if no document is given
                                    bw.write("HTTP/1.1 200 OK\r\n");
                                    bw.write("Server: " + SERVERNAME + "\r\n");
                                    bw.write("");
                                    bw.write("\r\n");
                                    bw.write("<!doctype html>\n<html>\n<body>\n");
                                    bw.write("<center><h1>Your Useragent</h1></center>\n");
                                    bw.write("<center><h2>" + getUserAgent(req) + "</h2></center>");
                                    bw.write("\n</body>");
                                    bw.write("\n</html>");
                                    System.out.printf("[%s] <= 200 OK\n", socket.getInetAddress().toString());
                                } else {
                                    int fileexist = fileExists(allargs[1]);
                                    if (fileexist == 1) {
                                        bw.write("HTTP/1.1 200 OK\r\n");
                                        bw.write("Server: " + SERVERNAME + "\r\n");
                                        bw.write("");
                                        bw.write("\r\n");
                                        bw.write(processHTML(readFile(allargs[1]), req));
                                        System.out.printf("[%s] <= 200 OK\n", socket.getInetAddress().toString());
                                    } else if (fileexist == 2){
                                        bw.write("HTTP/1.1 403 Forbidden\r\n");
                                        bw.write("Server: " + SERVERNAME + "\r\n");
                                        bw.write("");
                                        bw.write("\r\n");
                                        bw.write("<!doctype html>\n<html>\n<body>\n");
                                        bw.write("<center><h1>403 Forbidden</h1></center>");
                                        bw.write("<center><h3>You're not allowed to access " + allargs[1].replace("..", "").replaceFirst("/", "") + "!</center></h3>");
                                        bw.write("\n</body>");
                                        bw.write("\n</html>");
                                        System.out.printf("[%s] <= 403 Forbidden\n", socket.getInetAddress().toString());
                                        logError("[" + socket.getInetAddress().toString() + "] <= 403 Forbidden " + allargs[1]);
                                    } else {
                                        bw.write("HTTP/1.1 404 Not Found\r\n");
                                        bw.write("Server: " + SERVERNAME + "\r\n");
                                        bw.write("");
                                        bw.write("\r\n");
                                        bw.write("<!doctype html>\n<html>\n<body>\n");
                                        bw.write("<center><h1>404 Not Found</h1></center>");
                                        bw.write("<center><h3>File " + allargs[1].replace("..", "").replaceFirst("/", "") + " not found!</center></h3>");
                                        bw.write("\n</body>");
                                        bw.write("\n</html>");
                                        System.out.printf("[%s] <= 404 Not Found\n", socket.getInetAddress().toString());
                                        logError("[" + socket.getInetAddress().toString() + "] <= 403 Forbidden " + allargs[1]);
                                    }
                                }
                                break;
                            default:
                                bw.write("HTTP/1.1 400 Bad Request\r\n");
                                bw.write("Server: " + SERVERNAME + "\r\n");
                                bw.write("");
                                bw.write("\r\n");
                                bw.write("<!doctype html>\n<html>\n<body>\n");
                                bw.write("<center><h1>400 Bad Request</h1></center>");
                                bw.write("<center><h3>Request " + allargs[0] + " not supported</center></h3>");
                                bw.write("\n</body>");
                                bw.write("\n</html>");
                                break;
                        }

                        bw.close();
                        br.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
