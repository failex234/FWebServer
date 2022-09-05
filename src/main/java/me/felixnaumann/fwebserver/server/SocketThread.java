package me.felixnaumann.fwebserver.server;

import me.felixnaumann.fwebserver.model.ClientHeader;
import me.felixnaumann.fwebserver.utils.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class SocketThread implements Runnable {

    Socket socket;

    public SocketThread(Socket socket) {
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
                    DataOutputStream binaryOut = new DataOutputStream(socket.getOutputStream());

                    ArrayList<String> req = new ArrayList<>();

                    String line;
                    while ((line = br.readLine()) != null) {
                        req.add(line);
                        if (line.isEmpty()) {
                            break;
                        }
                    }
                    ClientHeader header = new ClientHeader(req);
                    Server.setCurrentHeader(header);

                    if(header.isHeadercorrupt()) {
                        ServerUtils.writeResponse(bw, 400,
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
                            LogUtils.Consolelogf("[%s] %s %s\n", socket.getInetAddress().toString(), header.getRequesttype(), header.getRequesteddocument());
                            LogUtils.logAccess("[" + socket.getInetAddress().toString() + "] " + header.getRequesttype() + " " + header.getRequesteddocument());
                            if (header.getRequesteddocument().equals("/") || FileUtils.fileExists(header.getRequesteddocument()) == 3) {
                                boolean indexfound = false;
                                String contents = "";
                                String indexfilename = "";
                                for (String file : Server.config.getIndexfiles()) {
                                    if (FileUtils.fileExists(header.getRequesteddocument() + "/" + file) == 1) {
                                        indexfilename = (new File(header.getRequesteddocument() + "/" + file)).getName();
                                        contents = HtmlUtils.processHTML(FileUtils.readFilePlain(header.getRequesteddocument() + "/" + file), header);
                                        indexfound = true;
                                        break;
                                    }
                                }
                                if (!indexfound && !Server.config.isNofileindex()) {
                                    contents = FileUtils.listFiles(header.getRequesteddocument(),header);
                                    ServerUtils.writeResponse(bw, 200, contents);
                                    LogUtils.Consolelogf("[%s] <= 200 OK\n", socket.getInetAddress().toString());
                                } else if (indexfound) {
                                    if (MiscUtils.getFileExtension(indexfilename).equals("pyfs")) {
                                        String reqid = MiscUtils.newRequestId();
                                        try {
                                            contents = FileUtils.interpretScriptFile(new File(header.getRequesteddocument() + "/index.pyfs"), header.getRequesteddocument() + "/index.pyfs", reqid);
                                            ServerUtils.writeResponse(bw, 200, contents);
                                            LogUtils.Consolelogf("[%s] <= 200 OK\n", socket.getInetAddress().toString());
                                        }
                                        catch (Exception e) {
                                            String error = MiscUtils.buildErrorPage(e, indexfilename);
                                            ServerUtils.writeResponse(bw, 500, error);
                                            LogUtils.Consolelogf("[%s] <= 500 Internal Server Error\n", socket.getInetAddress().toString());
                                        }
                                        Server.scriptresults.remove(reqid);
                                        Server.scriptheader.remove(reqid);
                                    } else {
                                        ServerUtils.writeResponse(bw, 200, contents);
                                        LogUtils.Consolelogf("[%s] <= 200 OK\n", socket.getInetAddress().toString());
                                    }
                                } else {
                                    ServerUtils.writeResponse(bw, 403,
                                            "<!doctype html>\n<html>\n<body>\n",
                                            "<center><h1>403 Forbidden</h1></center>",
                                            "<center><h3>You're not allowed to access " + header.getRequesteddocument().replace("..", "") + "!</center></h3>",
                                            "\n<center><hr>\n " + Server.SERVERNAME + (!Server.config.isVersionSuppressed() ? "/" + Server.SERVERVERSION : "") + " on " + System.getProperty("os.name") + " at " + header.getHost() + "</center>",
                                            "\n</body>",
                                            "\n</html>");
                                    LogUtils.Consolelogf("[%s] <= 403 Forbidden\n", socket.getInetAddress().toString());
                                }


                            } else {
                                int fileexist = FileUtils.fileExists(header.getRequesteddocument());
                                if (fileexist == 1) {
                                    if (MiscUtils.getFileExtension(header.getRequesteddocument()).equals("pyfs")) {
                                        String reqid = MiscUtils.newRequestId();
                                        String contents = "";
                                        try {
                                            contents = FileUtils.interpretScriptFile(new File(header.getRequesteddocument()), header.getRequesteddocument(), reqid);
                                            ServerUtils.writeResponse(bw, 200, HtmlUtils.processHTML(contents, header));
                                            LogUtils.Consolelogf("[%s] <= 200 OK\n", socket.getInetAddress().toString());
                                        }
                                        catch (Exception e) {
                                            String error = MiscUtils.buildErrorPage(e, header.getRequesteddocument());
                                            ServerUtils.writeResponse(bw, 500, error);
                                            LogUtils.Consolelogf("[%s] <= 500 Internal Server Error\n", socket.getInetAddress().toString());
                                        }
                                        Server.scriptresults.remove(reqid);
                                        Server.scriptheader.remove(reqid);
                                    } else if (MiscUtils.getFileExtension(header.getRequesteddocument()).endsWith(".html")){
                                        ServerUtils.writeResponse(bw, 200,
                                                HtmlUtils.processHTML(FileUtils.readFilePlain(header.getRequesteddocument()), header));
                                        LogUtils.Consolelogf("[%s] <= 200 OK\n", socket.getInetAddress().toString());
                                    } else {
                                        ServerUtils.writeBinaryResponse(binaryOut, 200, FileUtils.readBinaryFile(header.getRequesteddocument()));
                                        LogUtils.Consolelogf("[%s] <= 200 OK\n", socket.getInetAddress().toString());
                                    }
                                } else if (fileexist == 2){
                                    ServerUtils.writeResponse(bw, 403,
                                            "<!doctype html>\n<html>\n<body>\n",
                                            "<center><h1>403 Forbidden</h1></center>",
                                            "<center><h3>You're not allowed to access " + header.getRequesteddocument().replace("..", "") + "!</center></h3>",
                                            "\n<center><hr>\n " + Server.SERVERNAME + (!Server.config.isVersionSuppressed() ? "/" + Server.SERVERVERSION : "") + " on " + System.getProperty("os.name") + " at " + header.getHost() + "</center>",
                                            "\n</body>",
                                            "\n</html>");
                                    LogUtils.Consolelogf("[%s] <= 403 Forbidden\n", socket.getInetAddress().toString());
                                    LogUtils.logError("[" + socket.getInetAddress().toString() + "] <= 403 Forbidden " + header.getRequesteddocument());
                                } else if (fileexist == 3) {
                                    ServerUtils.writeResponse(bw, 200,
                                            "<!doctype html>\n",
                                            FileUtils.listFiles(header.getRequesteddocument(), header));
                                    LogUtils.Consolelogf("[%s] <= 200 OK\n", socket.getInetAddress().toString());
                                } else {
                                    ServerUtils.writeResponse(bw, 404,
                                            "<!doctype html>\n<html>\n<body>\n",
                                            "<center><h1>404 Not Found</h1></center>",
                                            "<center><h3>The requested url " + header.getRequesteddocument().replace("..", "") + " was not found!</center></h3>",
                                            "\n<center><hr>\n " + Server.SERVERNAME + (!Server.config.isVersionSuppressed() ? "/" + Server.SERVERVERSION : "") + " on " + System.getProperty("os.name") + " at " + header.getHost() + "</center>",
                                            "\n</body>",
                                            "\n</html>");
                                    LogUtils.Consolelogf("[%s] <= 404 Not Found\n", socket.getInetAddress().toString());
                                    LogUtils.logError("[" + socket.getInetAddress().toString() + "] <= 404 Not Found " + header.getRequesteddocument());
                                }
                            }
                            break;
                        default:
                            LogUtils.Consolelogf("[%s] %s %s\n", socket.getInetAddress(), header.getRequesttype(), header.getRequesteddocument());
                            LogUtils.logError("[" + socket.getInetAddress() + "] <= 501 Not Implemented");
                            ServerUtils.writeResponse(bw, 501,
                                    "<!doctype html>\n<html>\n<body>\n",
                                    "<center><h1>501 Not Implemented</h1></center>",
                                    "<center><h3>Request " + header.getRequesttype() + " not (yet) supported</center></h3>",
                                    "\n<center><hr>\n " + Server.SERVERNAME + (!Server.config.isVersionSuppressed() ? "/" + Server.SERVERVERSION : "") + " on " + System.getProperty("os.name") + " at " + header.getHost() + "</center>",
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
                    Server.setCurrentHeader(null);
                }

            }
        }
    }
}