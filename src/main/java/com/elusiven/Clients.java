package com.elusiven;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Clients implements ReceiveListener {

    private List<Client> clients = new ArrayList<Client>();

    private static Clients clientsInstance;

    public Clients(){
        clientsInstance = Clients.this;
    }

    public static Clients getInstance(){
        return clientsInstance;
    }

    // Add new player
    public void addClient(Client client){
        System.out.println("Adding new client --> ID: " + client.getId());
        if(!clients.contains(client))
        clients.add(client);
    }

    // Remove player
    @Override
    public void removeClient(Client client){
        clients.remove(client);
    }

    // Find client by id
    public Client findById(String id){
        return clients.stream().filter(c -> c.getId().equals(id)).findFirst().get();
    }

    @Override
    public void dataReceive(Client client, ByteBuffer data) throws IOException {

        MessageRoot msg = MessageRoot.getRootAsMessageRoot(data);

        if(msg.dataType() == Data.NONE){
            // Do nothing
            System.out.println("Nothing");
        } else if(msg.dataType() == Data.InitialConnectCommand){
            // Initial Connect Command
            InitialConnectCommand connCommand = (InitialConnectCommand)msg.data(new InitialConnectCommand());
            if(connCommand != null){
                client.setPosition(connCommand.player().pos());
                client.setId(connCommand.player().id());
                System.out.println("Player Ack Connection --> New ID: " + connCommand.player().id());
                System.out.println("Player Ack Connection --> Old ID: " + client.getId());
                //TODO Client connects to second server but we don't know how to match the ID
                //TODO of connected new client to the id of previous transfered same client
            }
        } else if(msg.dataType() == Data.MovementCommand){
            // Move player
            MovementCommand movCommand = (MovementCommand)msg.data(new MovementCommand());
            if (movCommand != null) {
                client.setPosition(movCommand.player().pos());
                client.setServerOwnerId(movCommand.player().serverId());
                checkServerBoundary(client);
                CheckAreaOfInterest(client, data);
                System.out.println("Moving Player --> ID: " + movCommand.player().id());
            }

        } else if(msg.dataType() == Data.MeetCommand){
            // player met another player
            MeetCommand meetCommand = (MeetCommand)msg.data(new MeetCommand());
            if(meetCommand != null){

            }
        } else if(msg.dataType() == Data.TransferPlayerCommand){
            // Transfer player to another server
            TransferPlayerCommand transferPlayerCommand = (TransferPlayerCommand)msg.data(new TransferPlayerCommand());
        } else {
            System.out.println("Empty command received.");
        }
    }

    // Check area of interest
    private void CheckAreaOfInterest(Client client, ByteBuffer data){

        Double distance = 0.0;

        for(Client item : clients){
            if(!item.equals(client)){
                distance = client.GetDistanceBetweenPlayers(item.getPosition());
                if(distance < 5 && !client.ContainsPlayer(item)){
                    System.out.println("Player ID: " + item.getId() + " entered area of interest (distance " + distance + ")  --> of Player ID " + client.getId());
                    client.addToAreaOfInterest(item);
                    item.addToAreaOfInterest(client);

                } else if(distance > 5 && client.ContainsPlayer(item)){
                    System.out.println("Player ID: " + item.getId() + " exit area of interest (distance " + distance + ")  --> of Player ID " + client.getId());
                    client.removeFromAreaOfInterest(item);
                    item.removeFromAreaOfInterest(client);
                }
            }
        }

        if(distance < 5){
            sendBroadcast(client, data.array());
        }
    }

    // Very simple checking for server boundary
    private void checkServerBoundary(Client client) throws IOException {

        // < 100 Map 1
        // > 100 Map 2

        // If client is in X of more or equal to 100 then transfer player to another server
        switch(Main.ServerID){
            case 1:{
                if(client.getPosition().x() >= 101 && clients.contains(client)){
                    Server server = Main.nearbyServers.findFirst();
                    client.sendTransferPlayerEvent(
                            Main.ServerIp,
                            27801,
                            client.getId(),
                            client.getPosition().x(),
                            client.getPosition().y(),
                            client.getPosition().z());
                    server.sendTransferPlayerEvent(
                            client.getId(),
                            client.getPosition().x(),
                            client.getPosition().y(),
                            client.getPosition().z());
                    System.out.println("Sent out a transfer command to another server --> Server: " + server);
                    clients.remove(client);
                }
            }
                break;
            case 2:{
                if(client.getPosition().x() <= 100 && clients.contains(client)){

                }
            }
                break;
        }
    }

    // Send broadcast to every client except origin
    private void sendBroadcast(Client client, byte[] data){
        for (Client item : clients){
            if(!item.equals(client)){
                item.sendToClient(data);
            }
        }
    }
}
