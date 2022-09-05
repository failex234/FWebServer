package me.felixnaumann.fwebserver.utils;

import me.felixnaumann.fwebserver.model.HttpStatus;
import me.felixnaumann.fwebserver.server.Server;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ServerUtils {
    /**
     * Common method to write the response header
     * @param bw The Buffered Writer to write to
     * @param status the http response status code
     * @param response the text that should also get sent in the response
     * @throws IOException
     */
    public static void writeResponse(BufferedWriter bw, int status, String... response) throws IOException {
        String code = HttpStatus.getText(status);

        if (code != null) {
            bw.write("HTTP/1.1 " + status + " " + code + "\r\n");
            bw.write("Server: " + Server.SERVERNAME + "\r\n");

            bw.write("Content-Type: " + Server.wantedfilemime + "\r\n");
            SimpleDateFormat sdf = new SimpleDateFormat("EE, dd MMM YYYY HH:mm:ss zz", Locale.ENGLISH);

            bw.write("Date: " + sdf.format(new Date()) + "\r\n");
            if (Server.wantedfileLastModified != null) {
                bw.write("Last-Modified: " + sdf.format(Server.wantedfileLastModified) + "\r\n");
            }

            bw.write("");
            bw.write("\r\n");
            for(String resp : response) {
                bw.write(resp);
            }
        }

        Server.wantedfilemime = "text/html";
        Server.wantedfileLastModified = null;
    }

    /**
     * Common method to write the response header
     * @param outputStream The output stream
     * @param status the http response status code
     * @param response the text that should also get sent in the response
     * @throws IOException
     */
    public static void writeBinaryResponse(DataOutputStream outputStream, int status, byte[] response) throws IOException {
        String code = HttpStatus.getText(status);

        if (code != null) {
            outputStream.writeBytes("HTTP/1.1 " + status + " " + code + "\r\n");
            outputStream.writeBytes("Server: " + Server.SERVERNAME + "\r\n");

            outputStream.writeBytes("Content-Type: " + Server.wantedfilemime + "\r\n");
            outputStream.writeBytes("Content-Length: " + response.length + "\r\n");
            SimpleDateFormat sdf = new SimpleDateFormat("EE, dd MMM YYYY HH:mm:ss zz", Locale.ENGLISH);

            outputStream.writeBytes("Date: " + sdf.format(new Date()) + "\r\n");
            if (Server.wantedfileLastModified != null) {
                outputStream.writeBytes("Last-Modified: " + sdf.format(Server.wantedfileLastModified) + "\r\n");
            }

            outputStream.writeBytes("\r\n");
            outputStream.write(response);
        }

        Server.wantedfilemime = "text/html";
        Server.wantedfileLastModified = null;
    }

    public static void sendErrorResponse(BufferedWriter bw, int status, String host, String doc) {
        try {
            ServerUtils.writeResponse(bw, status,
                    String.format(
                            "<!doctype html>\n<html>\n<body>\n" +
                                    "<center><h1>%d %s</h1></center>\n" +
                                    "<center><h3>%s</center></h3>\n" +
                                    "<center><hr>%s on %s at %s</center>\n" +
                                    "</body>\n</html>",
                            status,
                            HttpStatus.getText(status),
                            HttpStatus.getStatus(status).getErrortext(doc),
                            Server.SERVERNAME + (!Server.config.isVersionSuppressed() ? "/" + Server.SERVERVERSION : ""),
                            System.getProperty("os.name"),
                            host
                            ));
        }
        catch (IOException ignored) {

        }
    }


}
