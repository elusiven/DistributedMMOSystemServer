package com.elusiven;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static NearbyServers nearbyServers = new NearbyServers();

    public static int ServerID;
    public static String ServerIp;
    public static int ServerPort;

    public static void main(String[] args) throws IOException {

        ServerType serverType;
        // This server PORT
        int serverId = Integer.parseInt(args[0]);
        int serverPort = Integer.parseInt(args[1]);
        int clientPort = Integer.parseInt(args[2]);
        serverType = ServerType.values()[Integer.parseInt(args[3])];

        ServerID = serverId;
        ServerIp = "127.0.0.1";
        ServerPort = clientPort;

        // Start listening for other nearby servers
        ServerListener serverListener = new ServerListener(serverPort);
        serverListener.start();

        if(serverType == ServerType.SECONDARY) {
            // Connect to the primary server
            int primaryServerPort = 28900;
            Socket socket = new Socket("127.0.0.1", primaryServerPort);
            // Create server handler
            ServerHandler serverHandler = new ServerHandler();
            // Create reading thread for incoming messages from other servers
            Server secondaryServer = new Server(socket, serverHandler);
            System.out.println("Connected to primary server");
        }

        // *** CLIENT FUNCTIONS ***

        // Create new list of players
        Clients clients = new Clients();

        // Create socket for accepting client connections
        ServerSocket clientSocket = new ServerSocket(clientPort);

        // Keep accepting player connections
        while(true){
            System.out.println("Waiting for clients...");
            Socket socketClient = clientSocket.accept();
            Client client = new Client(socketClient, clients);
            clients.addClient(client);
            System.out.println("Client " + "-- "  + socketClient.getRemoteSocketAddress() + " --" + " connected.");
        }


    }

    public enum ServerType {
        PRIMARY, SECONDARY
    }
}
