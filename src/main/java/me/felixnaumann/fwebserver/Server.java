package me.felixnaumann.fwebserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.python.util.PythonInterpreter;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;

//TODO: pyfs zu python: Einrückung fixen!
//TODO: add optional setting to inject html tags into the response
public class Server {

    int port;
    private String SERVERNAME = "FWebServer";
    private final String VERSION = "0.3.1";
    private ServerSocket mainsocket;
    private Thread incoming;

    private ArrayList<String> blacklist = new ArrayList<>();
    private ArrayList<String> indexfiles = new ArrayList<>();

    private HashMap<String, GenericServerRunnable> specialkeywords = new HashMap<>();
    private HashMap<Integer, String> httpstatusCodes = new HashMap<>();

    private File accesslog;
    private File errorlog;
    private File configfile;
    private Gson gsoninstance;
    private ServerConfig config;

    private ClientHeader currentHeader = null;
    private boolean silenced;

    public static HashMap<String, StringBuilder> scriptresults = new HashMap<>();
    public static HashMap<String, ClientHeader> scriptheader = new HashMap<>();

    String wwwroot;
    String currdir;

    File wantedfile;
    String wantedfilemime;
    Date wantedfileLastModified;

    private static Server srv = null;

    public static Server getServerInstance(int port, boolean silenced) {
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
        prepareLog();
        prepareHTMLProcessor();
        this.port = port;
        this.silenced = silenced;
        start();
    }

    //TODO: uniform method for setting the whole respond header
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

        File webroot = new File(wwwroot);
        if (!webroot.exists()) {
            Consolelogf("The wwwroot path \"%s\" doesn't exist. creating it for you...\n", webroot.getAbsolutePath());
            webroot.mkdir();
            File aboutfile = new File(wwwroot + "/about2.html");
            File indexfile = new File(wwwroot + "/index.html");
            File dynfile = new File(wwwroot + "/about2dyn.pyfs");
            String filecontents = new String(Base64.getDecoder().decode(BuiltIn.about2));
            String indexcontents = new String(Base64.getDecoder().decode(BuiltIn.index));
            String dynfilecontents = new String(Base64.getDecoder().decode(BuiltIn.dyn));

            try {
                BufferedWriter bw = new BufferedWriter(new FileWriter(aboutfile));
                bw.write(filecontents);
                bw.close();

                bw = new BufferedWriter(new FileWriter(indexfile));
                bw.write(indexcontents);
                bw.close();

                bw = new BufferedWriter(new FileWriter(dynfile));
                bw.write(dynfilecontents);
                bw.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            //Check if config is null
            String status = config.isConfigCorrupt();
            if (!status.equals("no")) {
                System.err.println(status);
                System.exit(1);
            }
        }

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
        specialkeywords.put("accesslogpath", header -> accesslog.getAbsolutePath());
        specialkeywords.put("errorlogpath", header -> errorlog.getAbsolutePath());
        specialkeywords.put("configfilepath", header -> configfile.getAbsolutePath());
        specialkeywords.put("wwwrootpath", header -> (new File(wwwroot).getAbsolutePath()));
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
     * 0 = not exist, 1 = exists, 2 = no permissions, 3 = directory
     *
     * checks if a wanted file exists
     * @param filename the file to check
     * @return status code of the file
     */
    private int fileExists(String filename) throws IOException {
        if (filename.startsWith("..") || filename.startsWith("/")) {
            filename = filename.replace("..", "").replaceFirst("/", "");
        }

        File temp = new File( wwwroot + "/" + filename);
        File temp2 = new File(currdir + "/" + filename);

        if (!blacklist.contains(filename)) {
            if ((temp.exists() && temp.isDirectory()) || (temp2.exists() && temp2.isDirectory())) {
                if (temp.exists()) currdir = temp.getAbsolutePath();
                else currdir = temp2.getAbsolutePath();
                return 3;
            } else if (temp.exists() || temp2.exists()){
                if (temp.exists()) wantedfile = new File(temp.getAbsolutePath());
                else wantedfile = new File(temp2.getAbsolutePath());

                wantedfilemime = Files.probeContentType(wantedfile.toPath());
                if (wantedfilemime == null && filename.endsWith(".pyfs")) {
                    wantedfilemime = "text/html";
                }
                wantedfileLastModified = new Date(wantedfile.lastModified());
                return 1;
            }
            return 0;
        }
        return 2;
    }

    /**
     * Gets the parent directory of a file
     * @param currdocument the file that we want to know the parent directory of
     * @return the parent directory of a file or ..
     */
    private String getParentDirectory(String currdocument) {
        int slashpos = 0;
        for (int i = currdocument.length() - 1; i > 0; i--) {
            if (currdocument.charAt(i) == '/') {
                slashpos = i;
                break;
            }
        }
        if (slashpos != 0) return currdocument.substring(0, slashpos);
        return "..";
    }

    /**
     * list al the files in a given directory
     * @param path
     * @param header
     * @return
     */
    private String listFiles(String path, ClientHeader header) {
        File[] filelist = (new File(wwwroot + "/" + path).listFiles());
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n\t<head>\n\t\t<meta http-equiv=\"Content-Type\" content=\"text-html; charset=utf-8\"/>\n\t\t<title>Index of ").append(path).append("</title>\n\t</head>\n\t<body>\n");
        html.append("\t\t<h1>Index of ").append(path).append("</h1>\n");
        html.append("\t\t<table>\n");
        html.append("\t\t\t<tr>\n\t\t\t\t<td style=\"width: 300px;\">Name</td><td style=\"width: 300px;\">Last modified</td><td style=\"width: 300px;\">Size</td>\n\t\t\t</tr>");
        html.append("\n\t\t\t<tr>\n\t\t\t\t<td style=\"width: 300px;\"><a href=\"").append(getParentDirectory(header.getRequesteddocument())).append("\">..</a></td><td style=\"width: 300px;\"></td><td style=\"width: 300px;\"></td>\n\t\t\t</tr>");

        for (File dir : filelist) {
            if (dir.isFile()) continue;
            Date lastmodified = new Date(dir.lastModified());
            html.append("\n\t\t\t<tr>\n\t\t\t\t<td>").append("<a href=\"").append(path).append("/").append(dir.getName()).append("\">").append(dir.getName()).append("</a></td><td>").append(lastmodified.toString()).append("</td><td>").append("-</td>\n\t\t\t</tr>");
        }

        for (File f : filelist) {
            if (f.isDirectory()) continue;
            Date lastmodified = new Date(f.lastModified());
            html.append("\n\t\t\t<tr>\n\t\t\t\t<td>").append("<a href=\"").append(path).append("/").append(f.getName()).append("\">").append(f.getName()).append("</a></td><td>").append(lastmodified.toString()).append("</td><td>").append(convertToNearestUnit(f.length())).append("</td>\n\t\t\t</tr>");
        }
        html.append("\n\t\t</table>");
        html.append("\n\t\t<hr>\n\t\t$(servername)"+ (!config.isVersionSuppressed() ? "/" + VERSION : "")+"\n\t</body>\n</html>");

        return processHTML(html.toString(), header);
    }

    /**
     * Converts the number of bytes into the correct unit
     * @param size the number of bytes
     * @return the correctly formatted file size
     */
    private String convertToNearestUnit(long size) {
        StringBuilder endstring = new StringBuilder();
        double convertedsize;
        endstring.append("SIZE");
        if (size < 99L) {
            convertedsize = (double) size;
            endstring.append("B");
        } else if (size > 99L && size < 100000L) {
            convertedsize = (double) size / 1000D;
            endstring.append("K");
        } else if(size > 99999L && size < 100000000L) {
            convertedsize = (double) size / 1000000D;
            endstring.append("M");
        } else if (size > 99999999L && size < 100000000000L) {
            convertedsize = (double) size / 100000000D;
            endstring.append("G");
        } else if (size > 99999999999L && size < 100000000000000L) {
            convertedsize = (double) size / 1000000000000D;
            endstring.append("T");
        } else {
            convertedsize = (double) size / 10000000000000000D;
            endstring.append("P");
        }

        if (convertedsize == (long) convertedsize) return endstring.toString().replace("SIZE", String.format("%d", (int) convertedsize));
        return endstring.toString().replace("SIZE", String.format("%.2f", convertedsize));
    }

    /**
     * The HTML Processor replaces special keywords with results of functions
     * @param rawhtml the html from the file
     * @param header the client header
     * @return the processed HTML
     */
    private String processHTML(String rawhtml, ClientHeader header) {
        for(String keys : specialkeywords.keySet()) {
            rawhtml = rawhtml.replace("$(" + keys + ")", specialkeywords.get(keys).run(header));
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

        File toberead = wantedfile;
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

    /**
     * Tries to read the wanted scriptfile. Prints out an error to the browser when the read fails
     * @param file the scriptfile
     * @return contents of file
     */
    private String readScriptFile(File file) {
        StringBuilder tempstring = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while((line = br.readLine()) != null) {
                tempstring.append(line);
                tempstring.append("\n");
            }
            return tempstring.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "a.write('<font color=\"red\" size=\"120\">Error reading file</font>";
        }
    }

    /**
     * log to the console
     * @param string console text
     */
    private void Consolelog(String string) {
        if (!silenced) System.out.println("[LOG] " + string);
    }

    /**
     * log formatted to the console
     * @param format text format
     * @param objects
     */
    private void Consolelogf(String format, Object... objects) {
        if (!silenced) System.out.printf("[LOG] " + format, objects);
    }

    /**
     * Common method to write the response header
     * @param bw The Buffered Writer to write to
     * @param status the http response status code
     * @param response the text that should also get sent in the response
     * @throws IOException
     */
    private void writeResponse(BufferedWriter bw, int status, String... response) throws IOException {
        String code = httpstatusCodes.get(status);

        if (code != null) {
            bw.write("HTTP/1.1 " + status + " " + code + "\r\n");
            bw.write("Server: " + SERVERNAME + "\r\n");

            bw.write("Content-Type: " + wantedfilemime + "\r\n");
            SimpleDateFormat sdf = new SimpleDateFormat("EE, dd MMM YYYY HH:mm:ss zz", Locale.ENGLISH);

            bw.write("Date: " + sdf.format(new Date()) + "\r\n");
            if (wantedfileLastModified != null) {
                bw.write("Last-Modified: " + sdf.format(wantedfileLastModified) + "\r\n");
            }

            bw.write("");
            bw.write("\r\n");
            for(String resp : response) {
                bw.write(resp);
            }
        }

        wantedfilemime = "text/html";
        wantedfileLastModified = null;
    }

    /**
     * reads, converts html to python and then interprets the python.
     * @param scriptfile scriptfile to be run
     * @param relpath the relative path to the scriptfile (from the wwwroot directory)
     * @param rqid the request id to know which result corresponds to which client / request
     * @return the html of the interpreted scriptfile
     */
    private String interpretScriptFile(File scriptfile, String relpath, String rqid) {
        scriptresults.put(rqid, new StringBuilder());
        scriptheader.put(rqid, currentHeader);
        try {
            if (fileExists(relpath) == 1) {
                String contents = readScriptFile(wantedfile);

                boolean htmlfound = false;
                int startidx = 0, endidx = 0;

                //Extract html areas from script file
                while(contents.contains("\"\"\"html")) {
                    for (int i = 0; i < contents.length(); i++) {
                        if (i < contents.length() - 7) {
                            if (contents.substring(i, i + 7).equals("\"\"\"html")) {
                                htmlfound = true;
                                startidx = i + 7;
                                endidx = contents.length() - 1;
                            } else if (contents.substring(i, i + 7).equals("html\"\"\"")) {
                                if (htmlfound) {
                                    endidx = i - 1;
                                    contents = Utils.strCutAndConvert(contents, startidx, endidx, Utils.getCurrentLine(contents, i));
                                    htmlfound = false;
                                    break;
                                } else {
                                    //Count newlines to get the line number
                                    String linecounttemp = contents.substring(0, i + 1);
                                    int cnt = Utils.countChar(linecounttemp, '\n') + 1;

                                    contents = "a.write('<font color=\"red\" size=\"120\">Error: missing opening html tag (\"\"\"html) for closing tag (html\"\"\") in line " + cnt + "</font>";
                                }
                            }
                        }
                    }
                }

                PythonInterpreter pi = new PythonInterpreter();
                pi.exec("import me.felixnaumann.fwebserver.api.PythonApi as PythonApi");
                pi.exec("a = PythonApi(\"" + rqid + "\")");
                HashMap<String, String> getparams = Utils.getGETParams(currentHeader.getGETparams());
                String dict = Utils.constructPythonDictFromHashMap("GET", getparams);
                pi.exec(dict);
                pi.exec(contents);

                return scriptresults.get(rqid).toString();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public ClientHeader getCurrentHeader() {
        return currentHeader;
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
            //TODO implement timeout
            while (!Thread.currentThread().isInterrupted()) {
                while (!socket.isClosed()) {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));

                        ArrayList<String> req = new ArrayList<>();

                        String line;
                        while ((line = br.readLine()) != null) {
                            req.add(line);
                            if (line.isEmpty()) {
                                break;
                            }
                        }
                        ClientHeader header = new ClientHeader(req);
                        currentHeader = header;

                        if(header.isHeadercorrupt()) {
                            writeResponse(bw, 400,
                            "<!doctype html>\n<html>\n<body>",
                            "<center><h1>400 Bad Request</h1></center>",
                            "<center><h3>Invalid request header!</center></h3>",
                            "</body>",
                            "</html>");
                        }

                        switch (header.getRequesttype()) {
                            case "PUT":
                            case "POST":
                            case "DELETE":
                            case "PATCH":
                            case "GET":
                                Consolelogf("[%s] %s %s\n", socket.getInetAddress().toString(), header.getRequesttype(), header.getRequesteddocument());
                                logAccess("[" + socket.getInetAddress().toString() + "] " + header.getRequesttype() + " " + header.getRequesteddocument());
                                if (header.getRequesteddocument().equals("/") || fileExists(header.getRequesteddocument()) == 3) {
                                    boolean indexfound = false;
                                    String contents = "";
                                    String indexfilename = "";
                                    for (String file : config.getIndexfiles()) {
                                        if (fileExists(header.getRequesteddocument() + "/" + file) == 1) {
                                            indexfilename = (new File(header.getRequesteddocument() + "/" + file)).getName();
                                            contents = processHTML(readFile(header.getRequesteddocument() + "/" + file), header);
                                            indexfound = true;
                                            break;
                                        }
                                    }
                                    if (!indexfound && !config.isNofileindex()) {
                                        contents = listFiles(header.getRequesteddocument(),header);
                                        writeResponse(bw, 200, contents);
                                        Consolelogf("[%s] <= 200 OK\n", socket.getInetAddress().toString());
                                    } else if (indexfound) {
                                        if (Utils.getFileExtension(indexfilename).equals("pyfs")) {
                                            String reqid = Utils.newRequestId();
                                            try {
                                                contents = interpretScriptFile(new File(header.getRequesteddocument() + "/index.pyfs"), header.getRequesteddocument() + "/index.pyfs", reqid);
                                                writeResponse(bw, 200, contents);
                                                Consolelogf("[%s] <= 200 OK\n", socket.getInetAddress().toString());
                                            }
                                            catch (Exception e) {
                                                String error = Utils.buildErrorPage(e, indexfilename);
                                                writeResponse(bw, 500, error);
                                                Consolelogf("[%s] <= 500 Internal Server Error\n", socket.getInetAddress().toString());
                                            }
                                            scriptresults.remove(reqid);
                                            scriptheader.remove(reqid);
                                        } else {
                                            writeResponse(bw, 200, contents);
                                            Consolelogf("[%s] <= 200 OK\n", socket.getInetAddress().toString());
                                        }
                                    } else {
                                        writeResponse(bw, 403,
                                                "<!doctype html>\n<html>\n<body>\n",
                                                "<center><h1>403 Forbidden</h1></center>",
                                                "<center><h3>You're not allowed to access " + header.getRequesteddocument().replace("..", "") + "!</center></h3>",
                                                "\n<center><hr>\n " + SERVERNAME + (!config.isVersionSuppressed() ? "/" + VERSION : "") + " on " + System.getProperty("os.name") + " at " + header.getHost() + "</center>",
                                                "\n</body>",
                                                "\n</html>");
                                        Consolelogf("[%s] <= 403 Forbidden\n", socket.getInetAddress().toString());
                                    }


                                } else {
                                    int fileexist = fileExists(header.getRequesteddocument());
                                    if (fileexist == 1) {
                                        if (Utils.getFileExtension(header.getRequesteddocument()).equals("pyfs")) {
                                            String reqid = Utils.newRequestId();
                                            String contents = "";
                                            try {
                                                contents = interpretScriptFile(new File(header.getRequesteddocument()), header.getRequesteddocument(), reqid);
                                                writeResponse(bw, 200, processHTML(contents, header));
                                                Consolelogf("[%s] <= 200 OK\n", socket.getInetAddress().toString());
                                            }
                                            catch (Exception e) {
                                                String error = Utils.buildErrorPage(e, header.getRequesteddocument());
                                                writeResponse(bw, 500, error);
                                                Consolelogf("[%s] <= 500 Internal Server Error\n", socket.getInetAddress().toString());
                                            }
                                            scriptresults.remove(reqid);
                                            scriptheader.remove(reqid);
                                        } else {
                                            writeResponse(bw, 200,
                                                    processHTML(readFile(header.getRequesteddocument()), header));
                                            Consolelogf("[%s] <= 200 OK\n", socket.getInetAddress().toString());
                                        }
                                    } else if (fileexist == 2){
                                        writeResponse(bw, 403,
                                        "<!doctype html>\n<html>\n<body>\n",
                                        "<center><h1>403 Forbidden</h1></center>",
                                        "<center><h3>You're not allowed to access " + header.getRequesteddocument().replace("..", "") + "!</center></h3>",
                                        "\n<center><hr>\n " + SERVERNAME + (!config.isVersionSuppressed() ? "/" + VERSION : "") + " on " + System.getProperty("os.name") + " at " + header.getHost() + "</center>",
                                        "\n</body>",
                                        "\n</html>");
                                        Consolelogf("[%s] <= 403 Forbidden\n", socket.getInetAddress().toString());
                                        logError("[" + socket.getInetAddress().toString() + "] <= 403 Forbidden " + header.getRequesteddocument());
                                    } else if (fileexist == 3) {
                                        writeResponse(bw, 200,
                                        "<!doctype html>\n",
                                        listFiles(header.getRequesteddocument(), header));
                                        Consolelogf("[%s] <= 200 OK\n", socket.getInetAddress().toString());
                                    } else {
                                        writeResponse(bw, 404,
                                        "<!doctype html>\n<html>\n<body>\n",
                                        "<center><h1>404 Not Found</h1></center>",
                                        "<center><h3>The requested url " + header.getRequesteddocument().replace("..", "") + " was not found!</center></h3>",
                                        "\n<center><hr>\n " + SERVERNAME + (!config.isVersionSuppressed() ? "/" + VERSION : "") + " on " + System.getProperty("os.name") + " at " + header.getHost() + "</center>",
                                        "\n</body>",
                                        "\n</html>");
                                        Consolelogf("[%s] <= 404 Not Found\n", socket.getInetAddress().toString());
                                        logError("[" + socket.getInetAddress().toString() + "] <= 404 Not Found " + header.getRequesteddocument());
                                    }
                                }
                                break;
                            default:
                                Consolelogf("[%s] %s %s\n", socket.getInetAddress(), header.getRequesttype(), header.getRequesteddocument());
                                logError("[" + socket.getInetAddress() + "] <= 501 Not Implemented");
                                writeResponse(bw, 501,
                                "<!doctype html>\n<html>\n<body>\n",
                                "<center><h1>501 Not Implemented</h1></center>",
                                "<center><h3>Request " + header.getRequesttype() + " not (yet) supported</center></h3>",
                                "\n<center><hr>\n " + SERVERNAME + (!config.isVersionSuppressed() ? "/" + VERSION : "") + " on " + System.getProperty("os.name") + " at " + header.getHost() + "</center>",
                                "\n</body>",
                                "\n</html>");
                                break;
                        }

                        bw.close();
                        br.close();
                        socket.close();
                        Thread.currentThread().interrupt();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        currentHeader = null;
                    }

                }
            }
        }
    }
}
