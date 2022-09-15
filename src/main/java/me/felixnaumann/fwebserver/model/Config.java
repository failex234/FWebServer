package me.felixnaumann.fwebserver.model;

import me.felixnaumann.fwebserver.utils.ConfigUtils;

import java.util.HashMap;
import java.util.Map;

public class Config {
    private Map<String, String> mainconfig;
    private Map<String, HashMap<String, String>> hostconfig;

    public Config() {
        this.mainconfig = new HashMap<>();
        this.hostconfig = new HashMap<>();
    }

    public Config(Map<String, String> pMainconfig) {
        this.mainconfig = new HashMap<>(pMainconfig);
        this.hostconfig = new HashMap<>();
    }

    public void addHost(String name, Map<String, String> hostmap) {
        hostconfig.put(name, new HashMap<>(hostmap));
    }

    public String[] getHosts() {
        return hostconfig.keySet().toArray(new String[0]);
    }


    public int getInt(String keypath) {
        return Integer.parseInt(getString(keypath));
    }

    public boolean getBool(String keypath) {
        return Boolean.parseBoolean(getString(keypath));
    }

    public String getString(String keypath) {
        var cfgname = ConfigUtils.getConfigName(keypath);
        if (cfgname.equals("Main")) {
            return mainconfig.get(ConfigUtils.getKey(keypath));
        } else {
            var hostmap = hostconfig.get(ConfigUtils.getKey(keypath));
            return hostmap.get(ConfigUtils.getHostKey(keypath));
        }
    }

    public String[] getStringArr(String keypath) {
        var str = getString(keypath);
        return str.split(";");
    }


}
