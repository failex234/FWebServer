package me.felixnaumann.fwebserver.utils;

import me.felixnaumann.fwebserver.model.RequestHeader;
import me.felixnaumann.fwebserver.server.SpecialKeywords;
import me.felixnaumann.fwebserver.server.VirtualHost;

public class HtmlUtils {
    /**
     * The HTML Processor replaces special keywords with results of functions
     * @param rawhtml the html from the file
     * @param header the client header
     * @return the processed HTML
     */
    public static String replaceKeywords(String rawhtml, VirtualHost host, RequestHeader header) {
        return SpecialKeywords.getAllKeywords(header, host, rawhtml);
    }
}
