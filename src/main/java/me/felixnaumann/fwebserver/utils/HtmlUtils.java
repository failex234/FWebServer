package me.felixnaumann.fwebserver.utils;

import me.felixnaumann.fwebserver.model.ClientHeader;
import me.felixnaumann.fwebserver.server.Server;

public class HtmlUtils {
    /**
     * The HTML Processor replaces special keywords with results of functions
     * @param rawhtml the html from the file
     * @param header the client header
     * @return the processed HTML
     */
    public static String processHTML(String rawhtml, ClientHeader header) {
        for(String keys : Server.specialkeywords.keySet()) {
            rawhtml = rawhtml.replace("$(" + keys + ")", Server.specialkeywords.get(keys).run(header));
        }
        return rawhtml;
    }
}
