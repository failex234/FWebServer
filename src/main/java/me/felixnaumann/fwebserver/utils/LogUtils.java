package me.felixnaumann.fwebserver.utils;

import me.felixnaumann.fwebserver.FWebServer;
import me.felixnaumann.fwebserver.model.HttpStatus;
import me.felixnaumann.fwebserver.server.VirtualHost;

import java.io.*;

public class LogUtils {
    private static boolean _silenced;
    /**
     * log to the console
     * @param string console text
     */
    public static void consolelog(String string) {
        if (!_silenced) System.out.println("[LOG] " + string);
    }

    /**
     * log formatted to the console
     * @param format text format
     * @param objects
     */
    public static void consolelogf(String format, Object... objects) {
        if (!_silenced) System.out.printf("[LOG] " + format, objects);
    }


    /**
     *  Log a HTTP response with the corresponding message
     */
    public static void logResponse(int status, String endpoint, String doc, String reqid) {
        String httptext = HttpStatus.getText(status);
        LogUtils.consolelogf("[%s] (Request %s) <= %d %s\n", endpoint, reqid, status, httptext);

        if (status >= HttpStatus.HTTP_BAD_REQ.toInt()) {
            LogUtils.logError(String.format("[%s] (Request %s) <= %d %s %s", endpoint, reqid, status, httptext, doc));
        }
    }

    /**
     *  Log a HTTP response with the corresponding error message
     */
    public static void logResponse(int status, String endpoint, String reqid) {
        String httptext = HttpStatus.getText(status);
        LogUtils.consolelogf("[%s] (Request %s) <= %d %s\n", endpoint, reqid, status, httptext);

        if (status >= HttpStatus.HTTP_BAD_REQ.toInt()) {
            LogUtils.logError(String.format("[%s] <= %d %s", endpoint, status, httptext));
        }
    }

    /**
     * Log a http request to the console.
     * @param method
     * @param endpoint
     * @param doc
     */
    public static void logRequest(String method, String endpoint, String doc, String reqid) {
        LogUtils.consolelogf("[%s] (Request %s) %s %s\n", endpoint, reqid, method, doc);
        LogUtils.logAccess(String.format("[%s] %s %s", endpoint, method, doc));
    }

    /**
     * adds a string to the access log
     * @param tolog the string to append
     */
    public static void logAccess(String tolog) {
        try {
            FileWriter fw = new FileWriter(FWebServer.mainConfig.getAccesslog(), true);
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
            FileWriter fw = new FileWriter(FWebServer.mainConfig.getErrorlog(), true);
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
        File logfolder = new File(FWebServer.mainConfig.getLogfolder());

        if (!logfolder.exists()) {
            logfolder.mkdir();
        }
        try {
            FWebServer.mainConfig.getAccesslog().createNewFile();
            FWebServer.mainConfig.getErrorlog().createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
