package chatgui;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            username = in.readLine();

            // Send existing online users to this new client FIRST
            Server.sendExistingUsers(this);

            // Then announce this user to everyone
            Server.broadcast("JOIN:" + username, this);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("TYPING:")) {
                    Server.broadcast(message, this);
                } else {
                    Server.broadcast(username + ": " + message, this);
                }
            }

        } catch (IOException e) {
            System.out.println(username + " disconnected.");
        } finally {
            Server.removeClient(this);
            Server.broadcast("LEAVE:" + username, this);
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}