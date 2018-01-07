package com.elusiven;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class NearbyServers implements ReceiveServerListener {

    private List<Server> nearbyServers = new ArrayList<>();

    public void addServer(Server server){
        nearbyServers.add(server);
    }

    public void removeServer(Server server){
        nearbyServers.remove(server);
    }

    public Server findById(int id){
        return nearbyServers.stream().filter(s -> s.getId() == id).findFirst().get();
    }

    public Server findFirst(){
        return nearbyServers.stream().findFirst().get();
    }

    public Server findByArea(Vec3 area){
        return nearbyServers.stream().filter(s -> s.getArea().equals(area)).findFirst().get();
    }

    @Override
    public void dataReceive(Server server, ByteBuffer data) {

        MessageRoot msg = MessageRoot.getRootAsMessageRoot(data);

        if(msg.dataType() == Data.TransferPlayerCommand) {
            // Transfer player to another server
            TransferPlayerCommand transferPlayerCommand = (TransferPlayerCommand) msg.data(new TransferPlayerCommand());
            System.out.println("Msg From Server: " + transferPlayerCommand.player().serverId() + "Player has been transferred -> ID: " + transferPlayerCommand.player().id());
        }
    }

    @Override
    public void removeServerSocket(Server server) {

    }
}
