package me.felixnaumann.fwebserver.api;

import me.felixnaumann.fwebserver.ClientHeader;
import me.felixnaumann.fwebserver.Request;
import me.felixnaumann.fwebserver.Server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

public class PythonApi {
    private String requestid;
    private Request req;

    private static HashMap<String, Request> requests = new HashMap<>();
    private static HashMap<String, BufferedWriter> bwforreq = new HashMap<>();

    public PythonApi(String reqid) {
        this.requestid = reqid;
    }

    public static Request getRequest(String requestid) {
        return requests.get(requestid);
    }

    public static void addRequest(Request req) {
        requests.put(req.reqid, req);
    }

    public static void addBW(String reqid, BufferedWriter bw) {
        bwforreq.put(reqid, bw);
    }

    public void write(String line) {
        Server.scriptresults.replace(requestid, Server.scriptresults.get(requestid).append(line));
    }

    public String getRequestMethod() {
        return Server.getServerInstance(0, false).getCurrentHeader().getRequesttype();
    }
}
