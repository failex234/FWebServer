package me.felixnaumann.fwebserver.model;

public enum HttpStatus {
    HTTP_OK(200, "OK", ""),
    HTTP_CREATED(201, "Created", ""),
    HTTP_ACCEPTED(202, "Accepted", ""),
    HTTP_NO_CONTENT(204, "No Content", ""),
    HTTP_RESET_CONTENT(205, "Reset Content", ""),
    HTTP_PART_CONTENT(206, "Partial Content", ""),
    HTTP_MOVD_PERM(301, "Moved Permanently", ""),
    HTTP_FOUND(302, "Found (Moved Temporarily)", ""),
    HTTP_NOT_MOD(304, "Not Modified", ""),
    HTTP_BAD_REQ(400, "Bad Request", "Invalid request header!"),
    HTTP_UNAUTHD(401, "Unauthorized", "You are not authorized to access %s!"),
    HTTP_FORBIDDEN(403, "Forbidden", "You're not allowed to access %s!"),
    HTTP_NOT_FOUND(404, "Not Found", "The requested url %s was not found!"),
    HTTP_LENGTH_REQD(411, "Length Required", ""),
    HTTP_REQ_ENT_TOO_LARGE(413, "Request Entity Too Large", ""),
    HTTP_INT_SERV_ERR(500, "Internal Server Error", ""),
    HTTP_NOT_IMPL(501, "Not Implemented", "Request %s not (yet) supported"),
    ;

    private final int status;
    private final String text;
    private final String errortext;

    HttpStatus(final int status, final String text, final String errortext) {
        this.status = status;
        this.text = text;
        this.errortext = errortext;
    }

    @Override
    public String toString() {
        return text;
    }

    public int toInt() { return status; }


    public String getErrortext(String doc) {
        if (this.errortext.contains("%s")) {
            return String.format(errortext, doc);
        }
        return errortext;
    }



    public static String getText(int status) {
        for (HttpStatus httpStatus: HttpStatus.values()) {
            if (httpStatus.status == status) return httpStatus.text;
        }
        return "";
    }

    public static HttpStatus getStatus(int status) {
        for (HttpStatus httpStatus: HttpStatus.values()) {
            if (httpStatus.status == status) return httpStatus;
        }
        return HTTP_OK;
    }
}
