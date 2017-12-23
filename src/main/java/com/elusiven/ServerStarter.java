package com.elusiven;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerStarter extends ServerSocket {


    public ServerStarter(int port) throws IOException {
        super(port);
    }
}
