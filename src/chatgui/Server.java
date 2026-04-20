package chatgui;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    private static final int PORT = 6000;
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Chat server started on port " + PORT);
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket);
            clients.add(handler);
            new Thread(handler).start();
        }
    }

    public static synchronized void sendExistingUsers(ClientHandler newClient) {
        for (ClientHandler client : clients) {
            if (client != newClient && client.getUsername() != null) {
                newClient.sendMessage("JOIN:" + client.getUsername());
            }
        }
    }

    public static synchronized void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public static synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }
}