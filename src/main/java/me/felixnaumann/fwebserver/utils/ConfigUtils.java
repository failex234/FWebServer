package me.felixnaumann.fwebserver.utils;

import me.felixnaumann.fwebserver.model.Config;
import org.ini4j.Ini;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class ConfigUtils {
    private static File configfile = new File("config.ini");

    public static Config loadConfig() {
        if (configfile.exists()) {
            try {
                Ini ini = new Ini();
                ini.load(new FileReader(configfile));

                Ini.Section mainsec = ini.get("Main");

                //Create new config object with populated main section
                Config cfg = new Config(mainsec);

                //Populate config with all host sections
                for (var key : ini.keySet()) {
                    if (key.startsWith("Host.")) {
                        cfg.addHost(getHostKey(key), ini.get(key));
                    } else if (key.equals("Keywords")) {
                        Map<String, String> keywords = ini.get(key);
                        for (String keyword : keywords.keySet()) {
                            cfg.addKeyword(keyword, keywords.get(keyword));
                        }
                    } else if (key.equals("Headers")) {
                        Map<String, String> headers = ini.get(key);
                        for (String header : headers.keySet()) {
                            cfg.addHeader(header, headers.get(header));
                        }
                    }
                }

                return cfg;
            }
            catch (IOException ignored) {}
        }
        return new Config();
    }

    public static String getConfigName(String keypath) {
        return keypath.substring(0, keypath.indexOf('.'));
    }

    public static String getKey(String keypath) {
        String substr = keypath.substring(keypath.indexOf('.') + 1);
        if (substr.contains(".")) return substr.substring(0, substr.indexOf("."));
        return substr;
    }

    public static String getHostKey(String keypath) {
        var key = getKey(keypath);
        return keypath.substring(keypath.lastIndexOf('.') + 1);
    }
}