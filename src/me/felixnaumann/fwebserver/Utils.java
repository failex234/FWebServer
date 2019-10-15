package me.felixnaumann.fwebserver;

import org.python.antlr.ast.Str;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

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

    public static String htmlTopython(String lines) {
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

    public static String strCutAndConvert(String str, int from, int to) {
        String before = str.substring(0, from - 7);
        String converted = htmlTopython(str.substring(from + 1, to));
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

}
