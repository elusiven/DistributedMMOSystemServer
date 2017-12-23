package com.elusiven;

import java.nio.ByteBuffer;

public interface ReceiveServerListener {
    void dataReceive(Server server, ByteBuffer data);
    void removeClient(Server server);
}
