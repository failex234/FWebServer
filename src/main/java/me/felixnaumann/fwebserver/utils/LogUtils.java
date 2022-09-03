package me.felixnaumann.fwebserver.utils;

import me.felixnaumann.fwebserver.server.Server;

import java.io.*;

public class LogUtils {
    private static boolean _silenced;
    /**
     * log to the console
     * @param string console text
     */
    public static void Consolelog(String string) {
        if (!_silenced) System.out.println("[LOG] " + string);
    }

    /**
     * log formatted to the console
     * @param format text format
     * @param objects
     */
    public static void Consolelogf(String format, Object... objects) {
        if (!_silenced) System.out.printf("[LOG] " + format, objects);
    }

    /**
     * adds a string to the access log
     * @param tolog the string to append
     */
    public static void logAccess(String tolog) {
        try {
            FileWriter fw = new FileWriter(Server.config.getAccesslog(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);

            out.println(tolog);
            out.close();
            bw.close();
            fw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * adds a string to the error log
     * @param toerrorlog the string to append
     */
    public static void logError(String toerrorlog) {
        try {
            FileWriter fw = new FileWriter(Server.config.getErrorlog(), true);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);

            out.println(toerrorlog);
            out.close();
            bw.close();
            fw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setSilenced(boolean pSilenced) {
        _silenced = pSilenced;
    }

    public static boolean isSilenced() {
        return _silenced;
    }

    /**
     * Create new log files if non exist
     */
    public static void prepareLog() {
        File logfolder = new File(Server.config.getLogfolder());

        if (!logfolder.exists()) {
            logfolder.mkdir();
        }
        try {
            Server.config.getAccesslog().createNewFile();
            Server.config.getErrorlog().createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
