package com.elusiven;

import java.nio.ByteBuffer;

public class ServerHandler implements ReceiveServerListener {

    private Clients clients;

    public ServerHandler(){
        clients = Clients.getInstance();
    }

    @Override
    public void dataReceive(Server server, ByteBuffer data) {

        MessageRoot msg = MessageRoot.getRootAsMessageRoot(data);

        if(msg.dataType() == Data.TransferPlayerCommand) {
            // Transfer player to another server
            TransferPlayerCommand transferPlayerCommand = (TransferPlayerCommand) msg.data(new TransferPlayerCommand());
            System.out.println("Msg From Server: " + transferPlayerCommand.player().serverId() + " // Player has been transferred -> ID: " + transferPlayerCommand.player().id());
        }
    }

    @Override
    public void removeServerSocket(Server server) {

    }
}
