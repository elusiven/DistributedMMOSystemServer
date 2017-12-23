package com.elusiven;

import com.google.flatbuffers.FlatBufferBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Server {

    private Socket otherServer;
    private ReceiveServerListener listener;
    private InputStream inputStream;
    private OutputStream outputStream;

    private String id;
    private Vec3 area;
    private int port;

    public Server(Socket otherServer, ReceiveServerListener listener) throws IOException {
        this.otherServer = otherServer;
        this.listener = listener;
        inputStream = otherServer.getInputStream();
        outputStream = otherServer.getOutputStream();
        this.area = area;
        this.id = id;
    }

    public Server(Socket otherServer) throws IOException {
        this.otherServer = otherServer;
        inputStream = otherServer.getInputStream();
        outputStream = otherServer.getOutputStream();
        this.area = area;
        this.id = id;
    }

    public String getId(){
        return this.id;
    }

    // Set & Get area of the server
    public void setArea(Vec3 area){
        this.area = area;
    }

    public Vec3 getArea(){
        return this.area;
    }

    public void sendTransferPlayerEvent(String id, float x, float y, float z){

        FlatBufferBuilder fbb = new FlatBufferBuilder(1024);

        int playerInfoOffset = FlatCreator.create_PlayerInfo(fbb, id, x, y, z, 0, 0, 0, 0);

        TransferPlayerCommand.startTransferPlayerCommand(fbb);
        TransferPlayerCommand.addPlayer(fbb, playerInfoOffset);
        int transferPlayerCommandOffset = TransferPlayerCommand.endTransferPlayerCommand(fbb);

        MessageRoot.startMessageRoot(fbb);
        MessageRoot.addDataType(fbb, Data.TransferPlayerCommand);
        MessageRoot.addData(fbb, transferPlayerCommandOffset);
        int msgRootOffset = MessageRoot.endMessageRoot(fbb);
        MessageRoot.finishMessageRootBuffer(fbb, msgRootOffset);

        byte[] buffer = fbb.sizedByteArray();
        sendToServer(buffer);
    }

    // Send data to other server
    public void sendToServer(byte[] data){
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

    // Read incoming data from another server
    private class ReadThread extends Thread {
        @Override
        public void run() {

            byte[] bytes = new byte[1024];

            while(!otherServer.isClosed()){
                try {

                    int data = inputStream.read(bytes);
                    if(data != -1){
                        ByteBuffer buff = ByteBuffer.wrap(bytes, 0, data);
                        listener.dataReceive(Server.this, buff);
                    }

                } catch (IOException e){
                    e.printStackTrace();
                    break;
                }
            }

            try {
                inputStream.close();
                outputStream.close();
                otherServer.close();
                System.out.println("ServerStarter ID: " + id + "   -> Disconnected");
                listener.removeClient(Server.this);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


}
