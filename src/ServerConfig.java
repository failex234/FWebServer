import java.util.ArrayList;
import java.util.HashMap;

public class ServerConfig {

    private HashMap<String, String> customkeywords;
    private ArrayList<String> indexfiles;
    private String servername;
    private String wwwroot;
    private String accesslog;
    private String errorlog;
    private String logfolder;

    void createNewConfig() {
        customkeywords = new HashMap<>();
        indexfiles = new ArrayList<>();
        indexfiles.add("index.html");
        servername = "FWebServer";
        wwwroot = "wwwroot";
        accesslog = "logs/access.log";
        errorlog = "logs/error.log";
        logfolder = "logs";
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

    public String getAccesslog() {
        return accesslog;
    }

    public void setAccesslog(String accesslog) {
        this.accesslog = accesslog;
    }

    public String getErrorlog() {
        return errorlog;
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
}
