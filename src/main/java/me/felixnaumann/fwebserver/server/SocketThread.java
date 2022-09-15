package me.felixnaumann.fwebserver.server;

import me.felixnaumann.fwebserver.model.Request;
import me.felixnaumann.fwebserver.model.RequestHeader;
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
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                    DataOutputStream binaryOut = new DataOutputStream(socket.getOutputStream());

                    Request clientRequest = Request.buildRequest(socket.getInputStream());
                    RequestHeader header = clientRequest.getRequestHeader();

                    if(clientRequest.getRequestHeader().isHeadercorrupt()) {
                        ServerUtils.sendErrorResponse(bw, 400, clientRequest.getRequestHeader().getHost(), clientRequest, "");
                    }

                    switch (header.getRequesttype()) {
                        case "PUT":
                        case "POST":
                        case "DELETE":
                        case "PATCH":
                        case "GET":
                            LogUtils.logRequest(header.getRequesttype(), socket.getInetAddress().toString(), header.getRequesteddocument(), clientRequest.getRequestId());
                            if (header.getRequesteddocument().equals("/") || FileUtils.fileExists(header.getRequesteddocument()) == 3) {
                                File indexfile = new File(FileUtils.findIndexFile(clientRequest));
                                byte[] contents;

                                if (!indexfile.exists() && !Server.config.isNofileindex()) {
                                    contents = FileUtils.listFiles(header.getRequesteddocument(),header);
                                    ServerUtils.writeBinaryResponse(binaryOut, clientRequest, 200, contents);
                                    LogUtils.logResponse(200, socket.getInetAddress().toString(), clientRequest.getRequestId());
                                } else if (indexfile.exists()) {
                                    if (MiscUtils.getFileExtension(indexfile.getName()).equals("pyfs")) {
                                        try {
                                            contents = FileUtils.interpretScriptFile(new File(header.getRequesteddocument() + "/index.pyfs"), header.getRequesteddocument() + "/index.pyfs", clientRequest);
                                            ServerUtils.writeBinaryResponse(binaryOut, clientRequest, 200, contents);
                                            LogUtils.logResponse(200, socket.getInetAddress().toString(), clientRequest.getRequestId());
                                        }
                                        catch (Exception e) {
                                            String error = MiscUtils.buildErrorPage(e, indexfile.getName());
                                            ServerUtils.writeResponse(bw, 500, clientRequest, error);
                                            LogUtils.logResponse(500, socket.getInetAddress().toString(), clientRequest.getRequestId());
                                        }
                                        Server.getInstance().scriptresults.remove(clientRequest.getRequestId());
                                        Server.getInstance().scriptheader.remove(clientRequest.getRequestId());
                                    } else {
                                        contents = FileUtils.readBinaryFile(indexfile);
                                        ServerUtils.writeBinaryResponse(binaryOut, clientRequest, 200, contents);
                                        LogUtils.logResponse(200, socket.getInetAddress().toString(), clientRequest.getRequestId());
                                    }
                                } else {
                                    ServerUtils.sendErrorResponse(bw, 403, header.getHost(), clientRequest, header.getRequesteddocument());
                                    LogUtils.logResponse(403, socket.getInetAddress().toString(), clientRequest.getRequestId());
                                }
                            } else {
                                int fileexist = FileUtils.fileExists(header.getRequesteddocument());
                                if (fileexist == 1) {
                                    header.setRequesteddocument(Server.config.getWwwroot() + "/" + header.getRequesteddocument());
                                    clientRequest.setWantedDocument(new File(header.getRequesteddocument()));
                                    if (MiscUtils.getFileExtension(header.getRequesteddocument()).equals("pyfs")) {
                                        String reqid = MiscUtils.newRequestId();
                                        byte[] contents;
                                        try {
                                            contents = FileUtils.interpretScriptFile(new File(header.getRequesteddocument()), header.getRequesteddocument(), clientRequest);
                                            ServerUtils.writeResponse(bw, 200, clientRequest, HtmlUtils.replaceKeywords(new String(contents), header));
                                            LogUtils.logResponse(200, socket.getInetAddress().toString(), clientRequest.getRequestId());
                                        }
                                        catch (Exception e) {
                                            String error = MiscUtils.buildErrorPage(e, header.getRequesteddocument());
                                            ServerUtils.writeResponse(bw, 500, clientRequest, error);
                                            LogUtils.logResponse(500, socket.getInetAddress().toString(), clientRequest.getRequestId());
                                        }
                                        Server.getInstance().scriptresults.remove(reqid);
                                        Server.getInstance().scriptheader.remove(reqid);
                                    } else if (MiscUtils.getFileExtension(header.getRequesteddocument()).equals("html")){
                                        ServerUtils.writeResponse(bw, 200, clientRequest,
                                                HtmlUtils.replaceKeywords(FileUtils.readFilePlain(clientRequest.getWantedDocument()), header));
                                        LogUtils.logResponse(200, socket.getInetAddress().toString(), clientRequest.getRequestId());
                                    } else {
                                        ServerUtils.writeBinaryResponse(binaryOut, clientRequest,200,  FileUtils.readBinaryFile(new File(clientRequest.getRequestHeader().getRequesteddocument())));
                                        LogUtils.logResponse(200, socket.getInetAddress().toString(), clientRequest.getRequestId());
                                    }
                                } else if (fileexist == 2){
                                    ServerUtils.sendErrorResponse(bw, 403, header.getHost(), clientRequest, header.getRequesteddocument());
                                    LogUtils.logResponse(403, socket.getInetAddress().toString(), header.getRequesteddocument());
                                } else if (fileexist == 3) {
                                    ServerUtils.writeBinaryResponse(binaryOut, clientRequest, 200,
                                            FileUtils.listFiles(header.getRequesteddocument(), header));
                                    LogUtils.logResponse(200, socket.getInetAddress().toString(), clientRequest.getRequestId());
                                } else {
                                    ServerUtils.sendErrorResponse(bw, 404, header.getHost(), clientRequest, header.getRequesteddocument());
                                    LogUtils.logResponse(404, socket.getInetAddress().toString(), header.getRequesteddocument());
                                }
                            }
                            break;
                        default:
                            LogUtils.logResponse(501, socket.getInetAddress().toString(), header.getRequesteddocument());
                            ServerUtils.sendErrorResponse(bw, 501, header.getHost(), clientRequest, header.getRequesteddocument());
                            socket.close();
                            break;
                    }
                    bw.close();
                    socket.close();
                } catch (IOException ignored) {}

            }
            IncomingThread.completeThread(Thread.currentThread());
        }
    }
}