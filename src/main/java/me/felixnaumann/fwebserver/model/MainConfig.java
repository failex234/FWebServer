package me.felixnaumann.fwebserver.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class MainConfig {

    private String servername;
    private String accesslog;
    private String errorlog;
    private String logfolder;
    private boolean suppressversion;
    private HashMap<String, String> customkeywords = new HashMap<>();

    public void createNewConfig() {
        accesslog = "access.log";
        errorlog = "error.log";
        logfolder = "logs";
        suppressversion = false;
    }

    public File getAccesslog() {
        return new File(logfolder + "/" + accesslog);
    }

    public void setAccesslog(String accesslog) {
        if (accesslog != null && !accesslog.isBlank()) this.accesslog = accesslog;
    }

    public File getErrorlog() {
        return new File(logfolder + "/" + errorlog);
    }

    public void setErrorlog(String errorlog) {
        if (errorlog != null && !errorlog.isBlank()) this.errorlog = errorlog;
    }

    public String getLogfolder() {
        return logfolder;
    }

    public void setLogfolder(String logfolder) {
        if (logfolder != null && !logfolder.isBlank()) this.logfolder = logfolder;
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

    public HashMap<String, String> getCustomkeywords() {
        return customkeywords;
    }

    public String getServername() {
        return servername;
    }

    public void setServername(String servername) {
        if (servername != null && !servername.isBlank()) this.servername = servername;
    }
}
