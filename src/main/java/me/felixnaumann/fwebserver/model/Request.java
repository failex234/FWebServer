package me.felixnaumann.fwebserver.model;

import me.felixnaumann.fwebserver.server.Server;
import me.felixnaumann.fwebserver.utils.FileUtils;
import me.felixnaumann.fwebserver.utils.MiscUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;

public class Request {

    private final String requestId;
    private final File wantedDocument;
    private final String wantedDocumentMime;
    private final RequestHeader requestHeader;

    private Request(String requestId, RequestHeader header) {
        this.requestId = requestId;

        String finaldocument = "/";
        if (header.getRequesteddocument().equals("/")) {
            for (String indexfile : Server.config.getIndexfiles()) {
                if (FileUtils.fileExists(Server.config.getWwwroot() + "/" + indexfile) == 1) {
                    finaldocument = Server.config.getWwwroot() + "/" + indexfile;
                }
            }
        } else {
            finaldocument = header.getRequesteddocument();
        }

        this.wantedDocument = new File(FileUtils.sandboxFilename(finaldocument));

        String mime = "text/html";
        try {
            mime = Files.probeContentType(this.wantedDocument.toPath());
        }
        catch (IOException ignored) {}

        this.wantedDocumentMime = mime;

        this.requestHeader = header;
    }

    public static Request buildRequest(InputStream inputStream) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

            ArrayList<String> req = new ArrayList<>();

            String line;
            while ((line = br.readLine()) != null) {
                req.add(line);
                if (line.isEmpty()) {
                    break;
                }
            }
            br.close();

            RequestHeader header = new RequestHeader(req);

            return new Request(MiscUtils.newRequestId(), header);
        }
        catch (IOException e) {
            return new Request("", new RequestHeader(new ArrayList<>()));
        }
    }

    public String getRequestId() {
        return requestId;
    }

    public File getWantedDocument() {
        return wantedDocument;
    }

    public String getWantedDocumentMime() {
        return wantedDocumentMime;
    }

    public RequestHeader getRequestHeader() {
        return requestHeader;
    }
}
