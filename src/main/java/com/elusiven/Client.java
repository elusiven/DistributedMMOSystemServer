package com.elusiven;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.flatbuffers.Table;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Client {

    private Socket client;
    private ReceiveListener listener;
    private InputStream inputStream;
    private OutputStream outputStream;

    private String id = UUID.randomUUID().toString();
    private Vec3 position;

    private List<Client> AreaOfInterest = new ArrayList<>();

    public Client(Socket client, ReceiveListener listener) throws IOException {
        this.client = client;
        this.listener = listener;
        inputStream = client.getInputStream();
        outputStream = client.getOutputStream();
        new ReadThread().start();
        position = new Vec3();
        sendSpawnEvent(id, 30, 0.50f, 10);
    }

    public void setPosition(Vec3 pos){
        position = pos;
    }

    public Vec3 getPosition(){
        return position;
    }

    public double GetDistanceBetweenPlayers(Vec3 otherPlayerPosition){

        float x = position.x() - otherPlayerPosition.x();
        float y = position.y() - otherPlayerPosition.y();
        float z = position.z() - otherPlayerPosition.z();

        return Math.sqrt(
                Math.pow(x, 2f) +
                Math.pow(y, 2f) +
                Math.pow(z, 2f));
    }

    // Add other player to area of interest
    public void addToAreaOfInterest(Client otherPlayer){
        sendMeetEvent(otherPlayer.getId(),
                otherPlayer.getPosition().x(),
                otherPlayer.getPosition().y(),
                otherPlayer.getPosition().z());
        AreaOfInterest.add(otherPlayer);
    }

    // Remove other player from area of interest
    public void removeFromAreaOfInterest(Client otherPlayer){
        sendMeetEvent(otherPlayer.getId(),
                otherPlayer.getPosition().x(),
                otherPlayer.getPosition().y(),
                otherPlayer.getPosition().z());
        AreaOfInterest.remove(otherPlayer);
    }

    public boolean ContainsPlayer(Client otherPlayer) {
        return AreaOfInterest.contains(otherPlayer);
    }

    // Send spawn event
    public void sendSpawnEvent(String id, float x, float y, float z) {

        FlatBufferBuilder fbb = new FlatBufferBuilder(1024);

        int playerInfoOffset = FlatCreator.create_PlayerInfo(fbb, id, x, y, z, 0, 0, 0, 0);

        InitialConnectCommand.startInitialConnectCommand(fbb);
        InitialConnectCommand.addPlayer(fbb, playerInfoOffset);
        int initialConnectionCommandOffset = InitialConnectCommand.endInitialConnectCommand(fbb);

        MessageRoot.startMessageRoot(fbb);
        MessageRoot.addDataType(fbb, Data.InitialConnectCommand);
        MessageRoot.addData(fbb, initialConnectionCommandOffset);
        int endOffset = MessageRoot.endMessageRoot(fbb);
        MessageRoot.finishMessageRootBuffer(fbb, endOffset);

        byte[] buff = fbb.sizedByteArray();
        sendToClient(buff);
    }

    public void sendMeetEvent(String id, float x, float y, float z){

        FlatBufferBuilder fbb = new FlatBufferBuilder(1024);

        int playerInfoOffset = FlatCreator.create_PlayerInfo(fbb, id, x, y, z, 0, 0, 0, 0);

        MeetCommand.startMeetCommand(fbb);
        MeetCommand.addOtherPlayer(fbb, playerInfoOffset);
        int meetCommandOffset = MeetCommand.endMeetCommand(fbb);

        MessageRoot.startMessageRoot(fbb);
        MessageRoot.addDataType(fbb, Data.MeetCommand);
        MessageRoot.addData(fbb, meetCommandOffset);
        int msgRootOffset = MessageRoot.endMessageRoot(fbb);
        MessageRoot.finishMessageRootBuffer(fbb, msgRootOffset);

        byte[] buff = fbb.sizedByteArray();
        sendToClient(buff);
    }

    public String getId(){
        return id;
    }

    // Send data to client
    public void sendToClient(byte[] data){
        try {
            // Set & send length of message
            byte[] len = Helpers.intToByteArray(data.length);
            outputStream.write(len, 0, 4);
            // Send the message
            outputStream.write(data, 0, data.length);

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    // Read incoming data from client
    private class ReadThread extends Thread {
        @Override
        public void run() {

            byte[] bytes = new byte[1024];

                while(!client.isClosed()){
                    try {

                        int data = inputStream.read(bytes);
                        if(data != -1){
                            ByteBuffer buff = ByteBuffer.wrap(bytes, 0, data);
                            buff.order(ByteOrder.LITTLE_ENDIAN);
                            listener.dataReceive(Client.this, buff);
                        }

                    } catch (IOException e){
                        e.printStackTrace();
                        break;
                    }
                }

            try {
                inputStream.close();
                outputStream.close();
                client.close();
                System.out.println("Client ID: " + id + "   -> Disconnected");
                listener.removeClient(Client.this);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
