package me.felixnaumann.fwebserver.model;

import me.felixnaumann.fwebserver.utils.FileUtils;
import me.felixnaumann.fwebserver.utils.MiscUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

public class Request {

    private final String requestId;
    private File wantedDocument;
    private final String wantedDocumentMime;
    private final RequestHeader requestHeader;

    private Request(String requestId, RequestHeader header) {
        this.requestId = requestId;

        this.wantedDocument = new File(FileUtils.sandboxFilename(header.getRequesteddocument()));

        String mime = "text/html";
        try {
            if (header.getRequesteddocument().endsWith("/")) {
                mime = Files.probeContentType(new File(this.wantedDocument + "index.html").toPath());
            } else {
                mime = Files.probeContentType(this.wantedDocument.toPath());
            }
        }
        catch (IOException ignored) {}

        if (mime == null) mime = "text/html";

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

    public void setWantedDocument(File f) {
        if (f.exists()) {
            this.wantedDocument = f;
        }
    }

    public String getWantedDocumentMime() {
        return wantedDocumentMime;
    }

    public RequestHeader getRequestHeader() {
        return requestHeader;
    }
}
