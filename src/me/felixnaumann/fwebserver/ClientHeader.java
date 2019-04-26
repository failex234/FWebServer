package me.felixnaumann.fwebserver;

import java.util.ArrayList;

public class ClientHeader {

    private String requesttype = "";
    private String requesteddocument = "";
    private String version = "";
    private String host = "";
    private String useragent = "";
    private ArrayList<String> encodings = new ArrayList<>();
    private boolean headercorrupt = false;

    public ClientHeader(ArrayList<String> header) {
        int line = 0;
        for (String elems : header) {
            //TODO Check for incomplete header
            try {
                String[] temp = elems.toLowerCase().split(" ");
                String[] tempNormal = elems.split(" ");
                if (line == 0) {
                    this.setRequesttype(tempNormal[0].toUpperCase());
                    this.setRequesteddocument(tempNormal[1]);
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
                }
            } catch (Exception e) {
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
}
