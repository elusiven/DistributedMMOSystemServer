package com.elusiven;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface ReceiveListener {
    void dataReceive(Client client, ByteBuffer data) throws IOException;
    void removeClient(Client client);
}
