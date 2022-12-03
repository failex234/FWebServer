package me.felixnaumann.fwebserver.utils;

import me.felixnaumann.fwebserver.FWebServer;
import me.felixnaumann.fwebserver.model.HttpStatus;
import me.felixnaumann.fwebserver.model.Request;
import me.felixnaumann.fwebserver.server.VirtualHost;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class ServerUtils {
    /**
     * Common method to write the response header
     * @param bw The Buffered Writer to write to
     * @param status the http response status code
     * @param response the text that should also get sent in the response
     * @throws IOException
     */
    public static void writeResponse(BufferedWriter bw, int status, Request request, String... response) throws IOException {
        String code = HttpStatus.getText(status);

        if (code != null) {
            bw.write("HTTP/1.1 " + status + " " + code + "\r\n");
            bw.write("Server: " + FWebServer.mainConfig.getServername() + "\r\n");

            bw.write("Content-Type: " + request.getWantedDocumentMime() + "\r\n");
            SimpleDateFormat sdf = new SimpleDateFormat("EE, dd MMM YYYY HH:mm:ss zz", Locale.ENGLISH);

            bw.write("Date: " + sdf.format(new Date()) + "\r\n");
            bw.write("Last-Modified: " + sdf.format((new Date(request.getWantedDocument().lastModified()))) + "\r\n");

            HashMap<String, String> customheaders = FWebServer.mainConfig.getCustomheaders();

            for (String customheader : customheaders.keySet()) {
                String value = customheaders.get(customheader);
                bw.write(customheader + ": " + value + "\r\n");
            }

            bw.write("");
            bw.write("\r\n");
            for(String resp : response) {
                bw.write(resp);
            }
        }
    }

    /**
     * Common method to write the response header
     * @param outputStream The output stream
     * @param status the http response status code
     * @param response the text that should also get sent in the response
     * @throws IOException
     */
    public static void writeBinaryResponse(DataOutputStream outputStream, Request request, VirtualHost host, int status, byte[] response) throws IOException {
        String code = HttpStatus.getText(status);

        if (code != null) {
            outputStream.writeBytes("HTTP/1.1 " + status + " " + code + "\r\n");
            outputStream.writeBytes("Server: " + FWebServer.mainConfig.getServername() + "\r\n");

            outputStream.writeBytes("Content-Type: " + request.getWantedDocumentMime() + "\r\n");
            outputStream.writeBytes("Content-Length: " + response.length + "\r\n");
            SimpleDateFormat sdf = new SimpleDateFormat("EE, dd MMM YYYY HH:mm:ss zz", Locale.ENGLISH);

            outputStream.writeBytes("Date: " + sdf.format(new Date()) + "\r\n");
            outputStream.writeBytes("Last-Modified: " + sdf.format((new Date(request.getWantedDocument().lastModified()))) + "\r\n");

            HashMap<String, String> customheaders = FWebServer.mainConfig.getCustomheaders();

            for (String customheader : customheaders.keySet()) {
                String value = customheaders.get(customheader);
                outputStream.writeBytes(customheader + ": " + value + "\r\n");
            }

            outputStream.writeBytes("\r\n");
            if (request.getWantedDocumentMime().equals("text/html")) {
                String replacedKeywords = HtmlUtils.replaceKeywords(new String(response), host, request.getRequestHeader());
                outputStream.write(replacedKeywords.getBytes(StandardCharsets.UTF_8));
            } else {
                outputStream.write(response);
            }
        }
    }

    public static void sendErrorResponse(BufferedWriter bw, int status, String host, Request request, String doc) {
        try {
            ServerUtils.writeResponse(bw, status,
                    request,
                    String.format(
                            "<!doctype html>\n<html>\n<body>\n" +
                                    "<center><h1>%d %s</h1></center>\n" +
                                    "<center><h3>%s</center></h3>\n" +
                                    "<center><hr>%s on %s at %s</center>\n" +
                                    "</body>\n</html>",
                            status,
                            HttpStatus.getText(status),
                            HttpStatus.getStatus(status).getErrortext(doc),
                            FWebServer.mainConfig.getServername() + (!FWebServer.mainConfig.isVersionSuppressed() ? "/" + FWebServer.VERSION : ""),
                            System.getProperty("os.name"),
                            host
                            ));
        }
        catch (IOException ignored) {}
    }


}
