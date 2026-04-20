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

    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            username = in.readLine();
            Server.broadcast("🟢 " + username + " joined the chat!", this);

            String message;
            while ((message = in.readLine()) != null) {
                Server.broadcast(username + ": " + message, this);
            }

        } catch (IOException e) {
            System.out.println(username + " disconnected.");
        } finally {
            Server.removeClient(this);
            Server.broadcast("🔴 " + username + " left the chat.", this);
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}