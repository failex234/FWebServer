package me.felixnaumann.fwebserver.api;

import me.felixnaumann.fwebserver.Server;

public class PythonApi {
    public String getRequestMethod() {
        return Server.getServerInstance(0, false).getCurrentHeader().getRequesttype();
    }
}
