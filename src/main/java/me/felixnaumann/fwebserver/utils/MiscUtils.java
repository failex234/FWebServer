package me.felixnaumann.fwebserver.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class MiscUtils {

    public static String newRequestId() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss:SSS");
        String dateasstring = sdf.format(new Date());

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(dateasstring.getBytes(StandardCharsets.UTF_8));

            return byteToString(hash);
        }
        catch (NoSuchAlgorithmException ignored) {

        }
        return "";
    }

    private static String byteToString(byte[] hash) {
        StringBuilder hashasstring = new StringBuilder();

        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);

            if (hex.length() == 1) {
                hashasstring.append('0');
            } else {
                hashasstring.append(hex);
            }
        }

        return hashasstring.toString();
    }

    public static String getFileExtension(String filename) {
        int extensionidx = filename.indexOf('.');
        if (extensionidx == 0 || extensionidx == filename.length() - 1) return "";

        return filename.substring(extensionidx + 1);
    }

    public static String htmlTopython(String lines, int currLine) {
        StringBuilder cmd = new StringBuilder();
        String replaced = lines.replace("\"", "\\\"").replace("\n", "\\n");
        if (countTrailingSpaces(replaced) + countTrailingTabs(replaced) > 0) {
            int spaces = countTrailingSpaces(replaced) + countTrailingTabs(replaced);
            cmd.append(replaced.substring(0, spaces));
            replaced = replaced.substring(spaces);
        }
        cmd.append("a.write(\"");
        cmd.append(replaced);
        cmd.append("\")");
        return cmd.toString();
    }

    public static String strCutAndConvert(String str, int from, int to, int currLine) {
        String before = str.substring(0, from - 7);
        String converted = htmlTopython(str.substring(from + 1, to), currLine);
        String after = str.substring(to + 8);

        StringBuilder sb = new StringBuilder();
        sb.append(before);
        sb.append(converted);
        sb.append(after);

        return sb.toString();
    }

    public static int countChar(String str, char ch) {
        int count = 0;
        for (char textch : str.toCharArray()) {
            if (textch == ch) count++;
        }
        return count;
    }

    public static int countTrailingSpaces(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != ' ') return i;
        }
        return 0;
    }

    public static int countTrailingTabs(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != '\t') return i;
        }
        return 0;
    }

    public static String getNthLine(String alllines, int linenum) {
        int newlinecount = 0;
        int startidx = 0, endidx = 0;
        boolean beginningfound = false;
        for (int i = 0; i < alllines.length(); i++) {
            if (alllines.charAt(i) == '\n' && !beginningfound) newlinecount++;
            if (beginningfound) endidx = i;

            if (newlinecount - 1 == linenum) {
                startidx = i;
                beginningfound = true;
            }
        }

        return alllines.substring(startidx, endidx);
    }

    public static int getCurrentLine(String alllines, int idx) {
        int newlines = 0;
        for (int i = 0; i < alllines.length(); i++) {
            if (alllines.charAt(i) == '\n') newlines++;
            if (i == idx) break;
        }
        return newlines + 1;
    }

    public static String getNChars(char ch, int charnum) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < charnum; i++) {
            sb.append(ch);
        }

        return sb.toString();
    }

    public static HashMap<String, String> getGETParams(String params) {

        String[] keysandvals = params.split("&");
        HashMap<String, String> GET = new HashMap<>();
        for (String combined : keysandvals) {
            String[] seperated = combined.split("=");
            if (seperated != null && seperated.length == 2) {
                GET.put(seperated[0], seperated[1]);
            }
        }

        return GET;
    }

    public static HashMap<String, String> getPOSTParams(String reqdoc) {
        //TODO
        return new HashMap<>();
    }

    public static String constructPythonDictFromHashMap(String dictname, HashMap<String, String> hm) {
        StringBuilder sb = new StringBuilder();
        sb.append(dictname + " = {\n");
        for (String key : hm.keySet()) {
            sb.append("    \"" + key + "\": \"" + hm.get(key) + "\",\n");
        }
        sb.append("}");

        return sb.toString();
    }

    public static String buildErrorPage(Exception e, String filename) {
        StringBuilder errorpage = new StringBuilder();
        errorpage.append("<html><body>");
        errorpage.append("<h2>Error while processing pyfs:</h2>");
        errorpage.append("<font color=\"red\">");

        String tempTrace = ExceptionUtils.getStackTrace(e);
        tempTrace = tempTrace.replace("<string>", filename).replace("<", "&lt;").replace(">", "&gt;").replace("\n ", "<br>\t").replace("\n", "<br>");
        tempTrace = tempTrace.replaceAll("\\tat.+", "").replace("\t","&nbsp;&nbsp;&nbsp;&nbsp;").replaceAll("\\r<br>+", "");

        errorpage.append(tempTrace);
        errorpage.append("</font>");
        errorpage.append("</body></html>");
        String error = errorpage.toString();
        e.printStackTrace();

        return error.replace("<module>", "module");
    }

    public static String URLdecode(String encoded) {
        try {
            return URLDecoder.decode(encoded, StandardCharsets.UTF_8.toString());
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encoded;
    }

}
