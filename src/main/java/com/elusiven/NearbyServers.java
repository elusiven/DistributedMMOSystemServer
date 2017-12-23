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

    public Server findById(String id){
        return nearbyServers.stream().filter(s -> s.getId() == id).findFirst().get();
    }

    public Server findByArea(Vec3 area){
        return nearbyServers.stream().filter(s -> s.getArea().equals(area)).findFirst().get();
    }

    @Override
    public void dataReceive(Server server, ByteBuffer data) {

    }

    @Override
    public void removeClient(Server server) {

    }
}
