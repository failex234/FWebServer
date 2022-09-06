package me.felixnaumann.fwebserver.server;

import me.felixnaumann.fwebserver.annotations.SpecialKeyword;
import me.felixnaumann.fwebserver.model.RequestHeader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;

public class SpecialKeywords {

    @SpecialKeyword("useragent")
    private static String _keywordUseragent(RequestHeader header) {
        return header.getUseragent();
    }

    @SpecialKeyword("serverversion")
    private static String _keywordServerVersion(RequestHeader header) {
        return Server.getInstance().VERSION;
    }

    @SpecialKeyword("servername")
    private static String _keywordServerName(RequestHeader header) {
        return Server.getInstance().NAME;
    }

    @SpecialKeyword("jreversion")
    private static String _keywordJREVersion(RequestHeader header) {
        return System.getProperty("java.version");
    }

    @SpecialKeyword("osname")
    private static String _keywordOsName(RequestHeader header) {
        return System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch");
    }

    @SpecialKeyword("username")
    private static String _keywordUserName(RequestHeader header) {
        return System.getProperty("user.name");
    }

    @SpecialKeyword("accesslogpath")
    private static String _keywordAccessLogpath(RequestHeader header) {
        return Server.config.getAccesslog().getAbsolutePath();
    }

    @SpecialKeyword("errorlogpath")
    private static String _keywordErrorLogpath(RequestHeader header) {
        return Server.config.getErrorlog().getAbsolutePath();
    }

    @SpecialKeyword("configfilepath")
    private static String _keywordConfigFilepath(RequestHeader header) {
        return Server.config.getConfigFile().getAbsolutePath();
    }

    @SpecialKeyword("wwwrootpath")
    private static String _keywordWwwRootpath(RequestHeader header) {
        return (new File(Server.config.getWwwroot()).getAbsolutePath());
    }

    @SpecialKeyword("today")
    private static String _keywordToday(RequestHeader header) {
        return (new Date()).toString();
    }

    @SpecialKeyword("headertype")
    private static String _keywordHeaderType(RequestHeader header) {
        return header.getRequesttype();
    }

    @SpecialKeyword("headerversion")
    private static String _keywordHeaderVersion(RequestHeader header) {
        return header.getVersion();
    }

    @SpecialKeyword("headerhost")
    private static String _keywordHeaderHost(RequestHeader header) {
        return header.getHost();
    }

    @SpecialKeyword("compiledate")
    private static String _keywordCompileDate(RequestHeader header) {
        try {
            File jarFile = new File (Server.getInstance().getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            return (new Date(jarFile.lastModified())).toString();
        }
        catch (URISyntaxException e) {
            return "ERROR";
        }
    }

    @SpecialKeyword("keywordlist")
    private static String _keywordList(RequestHeader header) {
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

    public static String getAllKeywords(RequestHeader header, String rawtext) {
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
