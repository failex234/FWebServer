package me.felixnaumann.fwebserver;

import me.felixnaumann.fwebserver.model.ClientHeader;

public interface GenericServerRunnable {
    String run(ClientHeader header);
}
