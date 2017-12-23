package com.elusiven;

import java.nio.ByteBuffer;

public interface ReceiveListener {
    void dataReceive(Client client, ByteBuffer data);
    void removeClient(Client client);
}
