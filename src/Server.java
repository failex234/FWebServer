import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Server {

    int port;
    final String SERVERNAME = "FWebServer";
    final String VERSION = "0.2.2";
    ServerSocket mainsocket;
    Thread incoming;
    ArrayList<String> blacklist = new ArrayList<>();
    HashMap<String, GenericServerRunnable> specialkeywords = new HashMap<>();
    File accesslog;
    File errorlog;
    File configfile;

    public Server(int port) {
        prepareLog();
        prepareHTMLProcessor();
        prepareConfig();
        this.port = port;
        start();
    }

    private void prepareConfig() {
        configfile = new File("server.json");
    }

    private void prepareHTMLProcessor() {
        specialkeywords.put("$(useragent)", this::getUserAgent);
        specialkeywords.put("$(serverversion)", header -> VERSION);
        specialkeywords.put("$(servername)", header -> SERVERNAME);
        specialkeywords.put("$(jreversion)", header -> System.getProperty("java.version"));
        specialkeywords.put("$(osname)", header -> System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        specialkeywords.put("$(username)", header -> System.getProperty("user.name"));
        specialkeywords.put("$(accesslogpath)", header -> accesslog.getAbsolutePath());
        specialkeywords.put("$(errorlogpath)", header -> accesslog.getAbsolutePath());
        specialkeywords.put("$(configfilepath)", header -> configfile.getAbsolutePath());
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
        specialkeywords.put("$(keywordlist)", header -> {
            StringBuilder endstring = new StringBuilder();

            for (String keys : specialkeywords.keySet()) {
                endstring.append(keys.replace("(", "&#40;").replace(")", "&#41;"));
                endstring.append("<br>");
            }
            return endstring.toString();
        });
    }

    private void prepareLog() {
        accesslog = new File("logs/access.log");
        errorlog = new File("logs/error.log");
        File logfolder = new File("logs");

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

    private void start() {
        try {
            mainsocket = new ServerSocket(port);
            incoming = new Thread(new IncomingThread());
            incoming.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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

    public String getUserAgent(ArrayList<String> header) {
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
     * @param filename
     * @return
     */
    private int fileExists(String filename) {
        if (filename.startsWith("..") || filename.startsWith("/")) {
            filename = filename.replace("..", "").replaceFirst("/", "");
        }

        File temp = new File(filename);

        if (!blacklist.contains(filename)) {
            if (temp.exists()) {
                return 1;
            }
            return 0;
        }
        return 2;
    }

    private String processHTML(String rawhtml, ArrayList<String> header) {
        for(String keys : specialkeywords.keySet()) {
            rawhtml = rawhtml.replace(keys, specialkeywords.get(keys).run(header));
        }
        return rawhtml;
    }

    private String readFile(String filename) {
        //TODO: Read binary on non plain-text file
        if (filename.startsWith("..") || filename.startsWith("/")) {
            filename = filename.replaceFirst("/", "").replace("..", "");
        }

        File toberead = new File(filename);
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
