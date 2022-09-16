package me.felixnaumann.fwebserver.server;

import me.felixnaumann.fwebserver.FWebServer;
import me.felixnaumann.fwebserver.annotations.SpecialKeyword;
import me.felixnaumann.fwebserver.model.RequestHeader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;

public class SpecialKeywords {

    @SpecialKeyword("useragent")
    private static String _keywordUseragent(RequestHeader header, VirtualHost host) {
        return header.getUseragent();
    }

    @SpecialKeyword("serverversion")
    private static String _keywordServerVersion(RequestHeader header, VirtualHost host) {
        return FWebServer.VERSION;
    }

    @SpecialKeyword("servername")
    private static String _keywordServerName(RequestHeader header, VirtualHost host) {
        return FWebServer.mainConfig.getServername();
    }

    @SpecialKeyword("jreversion")
    private static String _keywordJREVersion(RequestHeader header, VirtualHost host) {
        return System.getProperty("java.version");
    }

    @SpecialKeyword("osname")
    private static String _keywordOsName(RequestHeader header, VirtualHost host) {
        return System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch");
    }

    @SpecialKeyword("username")
    private static String _keywordUserName(RequestHeader header, VirtualHost host) {
        return System.getProperty("user.name");
    }

    @SpecialKeyword("accesslogpath")
    private static String _keywordAccessLogpath(RequestHeader header, VirtualHost host) {
        return FWebServer.mainConfig.getAccesslog().getAbsolutePath();
    }

    @SpecialKeyword("errorlogpath")
    private static String _keywordErrorLogpath(RequestHeader header, VirtualHost host) {
        return FWebServer.mainConfig.getErrorlog().getAbsolutePath();
    }

    @SpecialKeyword("configfilepath")
    private static String _keywordConfigFilepath(RequestHeader header, VirtualHost host) {
        return (new File("config.ini")).getAbsolutePath();
    }

    @SpecialKeyword("wwwrootpath")
    private static String _keywordWwwRootpath(RequestHeader header, VirtualHost host) {
        return (new File(host.getWwwRoot()).getAbsolutePath());
    }

    @SpecialKeyword("today")
    private static String _keywordToday(RequestHeader header, VirtualHost host) {
        return (new Date()).toString();
    }

    @SpecialKeyword("headertype")
    private static String _keywordHeaderType(RequestHeader header, VirtualHost host) {
        return header.getRequesttype();
    }

    @SpecialKeyword("headerversion")
    private static String _keywordHeaderVersion(RequestHeader header, VirtualHost host) {
        return header.getVersion();
    }

    @SpecialKeyword("headerhost")
    private static String _keywordHeaderHost(RequestHeader header, VirtualHost host) {
        return header.getHost();
    }

    @SpecialKeyword("compiledate")
    private static String _keywordCompileDate(RequestHeader header, VirtualHost host) {
        try {
            File jarFile = new File (host.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            return (new Date(jarFile.lastModified())).toString();
        }
        catch (URISyntaxException e) {
            return "ERROR";
        }
    }

    @SpecialKeyword("keywordlist")
    private static String _keywordList(RequestHeader header, VirtualHost host) {
        StringBuilder keywords = new StringBuilder();
        for (String keyword : getKeywordList().keySet()) {
            keywords.append(keyword).append("<br>");
        }

        return keywords.toString();
    }


    private static HashMap<String, Method> getKeywordList() {
        HashMap<String, Method> keywordmethods = new HashMap<>();

        Method[] allclassmethods = SpecialKeywords.class.getDeclaredMethods();

        for (Method m : allclassmethods) {
            SpecialKeyword methodAnnotation = m.getAnnotation(SpecialKeyword.class);
            if (methodAnnotation != null) keywordmethods.put(methodAnnotation.value(), m);
        }

        return keywordmethods;
    }

    public static String getAllKeywords(RequestHeader header, VirtualHost host, String rawtext) {
        try {
            HashMap<String, Method> keywords = getKeywordList();
            String replacetext = rawtext;

            for (String keyword : keywords.keySet()) {
                if (rawtext.contains(String.format("$(%s)", keyword))) {
                    replacetext = replacetext.replace(String.format("$(%s)", keyword), (String) keywords.get(keyword).invoke(null, header, host));
                }
            }

            for (String keyword : FWebServer.mainConfig.getCustomkeywords().keySet()) {
                if (rawtext.contains(String.format("$(%s)", keyword))) {
                    replacetext = replacetext.replace(String.format("$(%s)", keyword), FWebServer.mainConfig.getCustomkeywords().get(keyword));
                }
            }
            return replacetext;
        }
        catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        return rawtext;
    }
}
