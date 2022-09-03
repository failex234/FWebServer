package me.felixnaumann.fwebserver;

import me.felixnaumann.fwebserver.utils.MiscUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class ClientHeader {

    private String requesttype = "";
    private String requesteddocument = "";
    private String version = "";
    private String host = "";
    private String useragent = "";
    private ArrayList<String> encodings = new ArrayList<>();
    private String GETparams = "";
    private boolean headercorrupt = false;
    private HashMap<String, String> otherfields = new HashMap<>();

    public ClientHeader(ArrayList<String> header) {
        int line = 0;
        for (String elems : header) {
            //TODO Check for incomplete header
            try {
                String[] temp = elems.toLowerCase().split(" ");
                String[] tempNormal = elems.split(" ");
                if (line == 0) {
                    this.setRequesttype(tempNormal[0].toUpperCase());
                    String[] docwithoutparams = tempNormal[1].split("\\?");
                    this.setRequesteddocument(MiscUtils.URLdecode(docwithoutparams[0]));
                    if (docwithoutparams.length > 1) {
                        this.setGETparams(MiscUtils.URLdecode(docwithoutparams[1]));
                    }
                    this.setVersion(tempNormal[2].toUpperCase().replace("HTTP/", ""));
                } else if (temp[0].equals("host:")) {
                    this.setHost(tempNormal[1]);
                } else if (temp[0].equals("user-agent:")) {
                    for (String uagentelems : tempNormal) {
                        if (!uagentelems.equals("User-Agent:"))
                            this.setUseragent(this.getUseragent() + uagentelems + " ");
                    }
                } else if (temp[0].equals("accept-encoding:")) {
                    String encodingstr = elems.replaceAll("accept-encoding:|\\s","");
                    //encodings = encodingstr.split(",");
                } else {
                    otherfields.put(temp[0].split(":")[0], tempNormal.length == 1 ? "" : tempNormal[1]);
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.setHeadercorrupt(true);
            }
            line++;
        }

    }

    public String getRequesttype() {
        return requesttype;
    }

    public String getRequesteddocument() {
        return requesteddocument;
    }

    public String getVersion() {
        return version;
    }

    public String getHost() {
        return host;
    }

    public String getUseragent() {
        return useragent;
    }

    private void setRequesttype(String requesttype) {
        this.requesttype = requesttype;
    }

    private void setRequesteddocument(String requesteddocument) {
        this.requesteddocument = requesteddocument;
    }

    private void setVersion(String version) {
        this.version = version;
    }

    private void setHost(String host) {
        this.host = host;
    }

    private void setUseragent(String useragent) {
        this.useragent = useragent;
    }

    public boolean isHeadercorrupt() {
        return headercorrupt;
    }

    private void setHeadercorrupt(boolean headercorrupt) {
        this.headercorrupt = headercorrupt;
    }

    public String getGETparams() {
        return GETparams;
    }

    public void setGETparams(String GETparams) {
        this.GETparams = GETparams;
    }

    public HashMap<String, String> getOtherfields() {
        return this.otherfields;
    }
}
