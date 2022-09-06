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
                        ServerUtils.sendErrorResponse(bw, 400, header.getHost(), "");
                    }

                    switch (header.getRequesttype()) {
                        case "PUT":
                        case "POST":
                        case "DELETE":
                        case "PATCH":
                        case "GET":
                            LogUtils.logRequest(header.getRequesttype(), socket.getInetAddress().toString(), header.getRequesteddocument());
                            if (header.getRequesteddocument().equals("/") || FileUtils.fileExists(header.getRequesteddocument()) == 3) {
                                boolean indexfound = false;
                                String contents = "";
                                String indexfilename = "";
                                for (String file : Server.config.getIndexfiles()) {
                                    if (FileUtils.fileExists(header.getRequesteddocument() + "/" + file) == 1) {
                                        indexfilename = (new File(header.getRequesteddocument() + "/" + file)).getName();
                                        contents = HtmlUtils.replaceKeywords(FileUtils.readFilePlain(header.getRequesteddocument() + "/" + file), header);
                                        indexfound = true;
                                        break;
                                    }
                                }
                                if (!indexfound && !Server.config.isNofileindex()) {
                                    contents = FileUtils.listFiles(header.getRequesteddocument(),header);
                                    ServerUtils.writeResponse(bw, 200, contents);
                                    LogUtils.logResponse(200, socket.getInetAddress().toString());
                                } else if (indexfound) {
                                    if (MiscUtils.getFileExtension(indexfilename).equals("pyfs")) {
                                        String reqid = MiscUtils.newRequestId();
                                        try {
                                            contents = FileUtils.interpretScriptFile(new File(header.getRequesteddocument() + "/index.pyfs"), header.getRequesteddocument() + "/index.pyfs", reqid);
                                            ServerUtils.writeResponse(bw, 200, contents);
                                            LogUtils.logResponse(200, socket.getInetAddress().toString());
                                        }
                                        catch (Exception e) {
                                            String error = MiscUtils.buildErrorPage(e, indexfilename);
                                            ServerUtils.writeResponse(bw, 500, error);
                                            LogUtils.logResponse(500, socket.getInetAddress().toString());
                                        }
                                        Server.scriptresults.remove(reqid);
                                        Server.scriptheader.remove(reqid);
                                    } else {
                                        ServerUtils.writeResponse(bw, 200, contents);
                                        LogUtils.logResponse(200, socket.getInetAddress().toString());
                                    }
                                } else {
                                    ServerUtils.sendErrorResponse(bw, 403, header.getHost(), header.getRequesteddocument());
                                    LogUtils.logResponse(403, socket.getInetAddress().toString());
                                }


                            } else {
                                int fileexist = FileUtils.fileExists(header.getRequesteddocument());
                                if (fileexist == 1) {
                                    if (MiscUtils.getFileExtension(header.getRequesteddocument()).equals("pyfs")) {
                                        String reqid = MiscUtils.newRequestId();
                                        String contents = "";
                                        try {
                                            contents = FileUtils.interpretScriptFile(new File(header.getRequesteddocument()), header.getRequesteddocument(), reqid);
                                            ServerUtils.writeResponse(bw, 200, HtmlUtils.replaceKeywords(contents, header));
                                            LogUtils.logResponse(200, socket.getInetAddress().toString());
                                        }
                                        catch (Exception e) {
                                            String error = MiscUtils.buildErrorPage(e, header.getRequesteddocument());
                                            ServerUtils.writeResponse(bw, 500, error);
                                            LogUtils.logResponse(500, socket.getInetAddress().toString());
                                        }
                                        Server.scriptresults.remove(reqid);
                                        Server.scriptheader.remove(reqid);
                                    } else if (MiscUtils.getFileExtension(header.getRequesteddocument()).endsWith(".html")){
                                        ServerUtils.writeResponse(bw, 200,
                                                HtmlUtils.replaceKeywords(FileUtils.readFilePlain(header.getRequesteddocument()), header));
                                        LogUtils.logResponse(200, socket.getInetAddress().toString());
                                    } else {
                                        ServerUtils.writeBinaryResponse(binaryOut, 200, FileUtils.readBinaryFile(header.getRequesteddocument()));
                                        LogUtils.logResponse(200, socket.getInetAddress().toString());
                                    }
                                } else if (fileexist == 2){
                                    ServerUtils.sendErrorResponse(bw, 403, header.getHost(), header.getRequesteddocument());
                                    LogUtils.logResponse(403, socket.getInetAddress().toString(), header.getRequesteddocument());
                                } else if (fileexist == 3) {
                                    ServerUtils.writeResponse(bw, 200,
                                            "<!doctype html>\n",
                                            FileUtils.listFiles(header.getRequesteddocument(), header));
                                    LogUtils.logResponse(200, socket.getInetAddress().toString());
                                } else {
                                    ServerUtils.sendErrorResponse(bw, 404, header.getHost(), header.getRequesteddocument());
                                    LogUtils.logResponse(404, socket.getInetAddress().toString(), header.getRequesteddocument());
                                }
                            }
                            break;
                        default:
                            LogUtils.logResponse(501, socket.getInetAddress().toString(), header.getRequesteddocument());
                            ServerUtils.sendErrorResponse(bw, 501, header.getHost(), header.getRequesteddocument());
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