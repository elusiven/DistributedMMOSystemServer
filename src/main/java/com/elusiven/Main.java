package com.elusiven;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {


    public static void main(String[] args) throws IOException {

        ServerType serverType;
        // This server PORT
        int serverPort = 28900;

        serverType = ServerType.PRIMARY;
        // If this is primary server then start listening for other server connections
        // if it's secondary server we need to connect to primary server
        // ---------------
        // Groups of servers will be in 5, the first one will have lowest index therefore primary
        if(serverType == ServerType.PRIMARY){
            // Start listening for other nearby servers
            ServerListener serverListener = new ServerListener(serverPort);
            serverListener.start();
        } else {
            // Connect to the primary server
            int primaryServerPort = 28900;
            Socket socket = new Socket("127.0.0.1", primaryServerPort);
            Server primaryServer = new Server(socket);
        }

        // *** CLIENT FUNCTIONS ***

        // Create new list of players
        Clients clients = new Clients();

        // Create socket for accepting client connections
        ServerSocket clientSocket = new ServerSocket(27800);

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
