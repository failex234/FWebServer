package me.felixnaumann.fwebserver.utils;

import me.felixnaumann.fwebserver.model.Config;
import me.felixnaumann.fwebserver.server.Server;
import org.ini4j.Ini;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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
        return keypath.substring(keypath.indexOf('.') + 1);
    }

    public static String getHostKey(String keypath) {
        var key = getKey(keypath);
        return keypath.substring(keypath.indexOf('.') + 1);
    }
}