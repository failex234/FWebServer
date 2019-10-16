package me.felixnaumann.fwebserver.api;

import me.felixnaumann.fwebserver.ClientHeader;
import me.felixnaumann.fwebserver.Request;
import me.felixnaumann.fwebserver.Server;
import me.felixnaumann.fwebserver.Utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

public class PythonApi {
    private String requestid;


    public PythonApi(String reqid) {
        this.requestid = reqid;
    }


    public void write(String line) {
        Server.scriptresults.replace(requestid, Server.scriptresults.get(requestid).append(line));
    }

    public String getRequestMethod() {
        return Server.getServerInstance(0, false).getCurrentHeader().getRequesttype();
    }

    public boolean isGetSet(String getparam) {
        return Server.scriptheader.get(requestid).getGETparams().matches(getparam + "=.+");
    }

    public String URLdecode(String encoded) {
        return Utils.URLdecode(encoded);
    }
}
