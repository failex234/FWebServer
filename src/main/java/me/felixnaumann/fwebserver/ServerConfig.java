package me.felixnaumann.fwebserver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerConfig {

    private HashMap<String, String> customkeywords;
    private HashMap<String, String> customheader;
    private ArrayList<String> indexfiles;
    private String servername;
    private String wwwroot;
    private String accesslog;
    private String errorlog;
    private String logfolder;
    private boolean suppressversion;
    private boolean nofileindex;

    public void createNewConfig() {
        customkeywords = new HashMap<>();
        customheader = new HashMap<>();
        indexfiles = new ArrayList<>();
        indexfiles.add("index.html");
        servername = "FWebServer";
        wwwroot = "wwwroot";
        accesslog = "logs/access.log";
        errorlog = "logs/error.log";
        logfolder = "logs";
        suppressversion = false;
        nofileindex = false;
    }

    public String getServername() {
        return servername;
    }

    public void setServername(String servername) {
        this.servername = servername;
    }

    public HashMap<String, String> getCustomkeywords() {
        return customkeywords;
    }

    public void setCustomkeywords(HashMap<String, String> customkeywords) {
        this.customkeywords = customkeywords;
    }

    public String getWwwroot() {
        return wwwroot;
    }

    public void setWwwroot(String wwwroot) {
        this.wwwroot = wwwroot;
    }

    public File getAccesslog() {
        return new File(accesslog);
    }

    public void setAccesslog(String accesslog) {
        this.accesslog = accesslog;
    }

    public File getErrorlog() {
        return new File(errorlog);
    }

    public void setErrorlog(String errorlog) {
        this.errorlog = errorlog;
    }

    public String getLogfolder() {
        return logfolder;
    }

    public void setLogfolder(String logfolder) {
        this.logfolder = logfolder;
    }

    public ArrayList<String> getIndexfiles() {
        return indexfiles;
    }

    public void setIndexfiles(ArrayList<String> indexfiles) {
        this.indexfiles = indexfiles;
    }

    public String isConfigCorrupt() {
        if (indexfiles == null) {
            return "indexfile entry not found";
        } else if (servername == null) {
            return "servername entry not found";
        } else if (wwwroot == null) {
            return "wwwroot entry not found";
        } else if (accesslog == null) {
            return "accesslog entry not found";
        } else if (errorlog == null) {
            return "errorlog entry not found";
        } else if (logfolder == null) {
            return "logfolder entry not found";
        } else {
            return "no";
        }
    }

    public File getConfigFile() {
        return new File("server.json");
    }

    public boolean isVersionSuppressed() {
        return suppressversion;
    }

    public void setSuppressversion(boolean suppressversion) {
        this.suppressversion = suppressversion;
    }

    public boolean isNofileindex() {
        return nofileindex;
    }

    public void setNofileindex(boolean nofileindex) {
        this.nofileindex = nofileindex;
    }
}
