package me.felixnaumann.fwebserver.server;

import me.felixnaumann.fwebserver.annotations.SpecialKeyword;
import me.felixnaumann.fwebserver.model.ClientHeader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;

public class SpecialKeywords {

    @SpecialKeyword("useragent")
    private static String _keywordUseragent(ClientHeader header) {
        return header.getUseragent();
    }

    @SpecialKeyword("serverversion")
    private static String _keywordServerVersion(ClientHeader header) {
        return Server.VERSION;
    }

    @SpecialKeyword("servername")
    private static String _keywordServerName(ClientHeader header) {
        return Server.NAME;
    }

    @SpecialKeyword("jreversion")
    private static String _keywordJREVersion(ClientHeader header) {
        return System.getProperty("java.version");
    }

    @SpecialKeyword("osname")
    private static String _keywordOsName(ClientHeader header) {
        return System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch");
    }

    @SpecialKeyword("username")
    private static String _keywordUserName(ClientHeader header) {
        return System.getProperty("user.name");
    }

    @SpecialKeyword("accesslogpath")
    private static String _keywordAccessLogpath(ClientHeader header) {
        return Server.config.getAccesslog().getAbsolutePath();
    }

    @SpecialKeyword("errorlogpath")
    private static String _keywordErrorLogpath(ClientHeader header) {
        return Server.config.getErrorlog().getAbsolutePath();
    }

    @SpecialKeyword("configfilepath")
    private static String _keywordConfigFilepath(ClientHeader header) {
        return Server.config.getConfigFile().getAbsolutePath();
    }

    @SpecialKeyword("wwwrootpath")
    private static String _keywordWwwRootpath(ClientHeader header) {
        return (new File(Server.config.getWwwroot()).getAbsolutePath());
    }

    @SpecialKeyword("today")
    private static String _keywordToday(ClientHeader header) {
        return (new Date()).toString();
    }

    @SpecialKeyword("headertype")
    private static String _keywordHeaderType(ClientHeader header) {
        return header.getRequesttype();
    }

    @SpecialKeyword("headerversion")
    private static String _keywordHeaderVersion(ClientHeader header) {
        return header.getVersion();
    }

    @SpecialKeyword("headerhost")
    private static String _keywordHeaderHost(ClientHeader header) {
        return header.getHost();
    }

    @SpecialKeyword("compiledate")
    private static String _keywordCompileDate(ClientHeader header) {
        try {
            File jarFile = new File (Server.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            return (new Date(jarFile.lastModified())).toString();
        }
        catch (URISyntaxException e) {
            return "ERROR";
        }
    }

    @SpecialKeyword("keywordlist")
    private static String _keywordList(ClientHeader header) {
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

    public static String getAllKeywords(ClientHeader header, String rawtext) {
        try {
            HashMap<String, Method> keywords = getKeywordList();
            String replacetext = rawtext;

            for (String keyword : keywords.keySet()) {
                if (rawtext.contains(String.format("$(%s)", keyword))) {
                    replacetext = replacetext.replace(String.format("$(%s)", keyword), (String) keywords.get(keyword).invoke(null, header));
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
