package me.felixnaumann.fwebserver.api;


import me.felixnaumann.fwebserver.server.Server;
import me.felixnaumann.fwebserver.utils.MiscUtils;

import java.util.Base64;
import java.util.HashMap;

/**
 * Contains methods that can get called from pyfs files
 */
public class PythonApi {
    private String requestid;


    public PythonApi(String reqid) {
        this.requestid = reqid;
    }


    public void write(String line) {
        Server.scriptresults.replace(requestid, Server.scriptresults.get(requestid).append(line));
    }

    public String getRequestMethod() {
        return Server.getInstance(0, false).getCurrentHeader().getRequesttype();
    }

    public boolean isGetSet(String getparam) {
        return Server.scriptheader.get(requestid).getGETparams().matches(getparam + "=.+");
    }

    public HashMap<String, String> getHeaderFields() {
        return Server.scriptheader.get(requestid).getOtherfields();
    }

    public String URLdecode(String encoded) {
        return MiscUtils.URLdecode(encoded);
    }

    public String base64decode(String b64) {
        return new String(Base64.getDecoder().decode(b64));
    }

    public String base64encode(String raw) {
        return new String(Base64.getDecoder().decode(raw));
    }
}
