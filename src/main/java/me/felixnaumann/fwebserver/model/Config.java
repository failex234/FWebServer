package me.felixnaumann.fwebserver.model;

import me.felixnaumann.fwebserver.utils.ConfigUtils;

import java.util.HashMap;
import java.util.Map;

public class Config {
    private Map<String, String> mainconfig;
    private Map<String, HashMap<String, String>> hostconfig;

    private HashMap<String, String> keywords;
    private HashMap<String, String> headers;

    public Config() {
        this.mainconfig = new HashMap<>();
        this.hostconfig = new HashMap<>();
        this.keywords = new HashMap<>();
        this.headers = new HashMap<>();
    }

    public Config(Map<String, String> pMainconfig) {
        this.mainconfig = new HashMap<>(pMainconfig);
        this.hostconfig = new HashMap<>();
        this.keywords = new HashMap<>();
        this.headers = new HashMap<>();
    }

    public void addHost(String name, Map<String, String> hostmap) {
        hostconfig.put(name, new HashMap<>(hostmap));
    }

    public void addKeyword(String keywordname, String keywordval) {
        keywords.put(keywordname, keywordval);
    }

    public void addHeader(String headername, String headerval) {
        headers.put(headername, headerval);
    }

    public HashMap<String, String> getKeywords() {
        return this.keywords;
    }

    public HashMap<String, String> getHeaders() {
        return this.headers;
    }

    public String[] getHosts() {
        return hostconfig.keySet().toArray(new String[0]);
    }

    public HashMap<String, String> getHost(String host) {
        return hostconfig.get(host);
    }


    public int getInt(String keypath) {
        return Integer.parseInt(getString(keypath));
    }

    public boolean getBool(String keypath) {
        return Boolean.parseBoolean(getString(keypath));
    }

    public String getString(String keypath) {
        var cfgname = ConfigUtils.getConfigName(keypath);
        var ret = "";
        if (cfgname.equals("Main")) {
            ret =  mainconfig.get(ConfigUtils.getKey(keypath));
        } else {
            var hostmap = hostconfig.get(ConfigUtils.getKey(keypath));
            var wantedstr = hostmap.get(ConfigUtils.getHostKey(keypath));

            if (wantedstr != null) ret = wantedstr;
        }
        return ret;
    }

    public String[] getStringArr(String keypath) {
        var str = getString(keypath);
        return str.split(";");
    }


}
