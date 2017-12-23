package com.elusiven;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Clients implements ReceiveListener {

    private List<Client> clients = new ArrayList<Client>();

    public Clients(){

    }

    // Add new player
    public void addClient(Client client){
        clients.add(client);
    }

    // Remove player
    @Override
    public void removeClient(Client client){
        clients.remove(client);
        client = null;
    }

    @Override
    public void dataReceive(Client client, ByteBuffer data) {

        MessageRoot msg = MessageRoot.getRootAsMessageRoot(data);

        if(msg.dataType() == Data.NONE){
            // Do nothing
            System.out.println("Nothing");
        } else if(msg.dataType() == Data.InitialConnectCommand){
            // Initial Connect Command
            InitialConnectCommand connCommand = (InitialConnectCommand)msg.data(new InitialConnectCommand());
            if(connCommand != null){
                System.out.println("Player Spawned --> ID: " + connCommand.player().id());
                client.setPosition(connCommand.player().pos());
            }
        } else if(msg.dataType() == Data.MovementCommand){
            // Move player
            MovementCommand movCommand = (MovementCommand)msg.data(new MovementCommand());
            if (movCommand != null) {
                client.setPosition(movCommand.player().pos());
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
        } else {
            System.out.println("Empty command received.");
        }
    }

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

    private void sendBroadcast(Client client, byte[] data){
        for (Client item : clients){
            if(!item.equals(client)){
                item.sendToClient(data);
            }
        }
    }
}
