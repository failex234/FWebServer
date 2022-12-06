package me.felixnaumann.fwebserver.api;


import me.felixnaumann.fwebserver.FWebServer;
import me.felixnaumann.fwebserver.annotations.PythonApiInterface;
import me.felixnaumann.fwebserver.utils.MiscUtils;

import java.util.Base64;
import java.util.HashMap;

/**
 * Contains methods that can get called from pyfs files
 */
@PythonApiInterface(instanceNeeded = true)
public class PythonApi {
    private String requestid;


    public PythonApi(String reqid) {
        this.requestid = reqid;
    }


    public void write(Object line) {
        FWebServer.scriptresults.replace(requestid, FWebServer.scriptresults.get(requestid).append(line));
    }

    public String getRequestMethod() {
        return FWebServer.scriptheader.get(requestid).getRequesttype();
    }

    public boolean isGetSet(String getparam) {
        return FWebServer.scriptheader.get(requestid).getGETparams().matches(getparam + "=.+");
    }

    public HashMap<String, String> getHeaderFields() {
        return FWebServer.scriptheader.get(requestid).getOtherfields();
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
