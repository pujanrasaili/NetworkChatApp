import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static List<PrintWriter> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Server started on port " + PORT);
        ServerSocket serverSocket = new ServerSocket(PORT);

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("New client connected: " + socket.getInetAddress());
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            synchronized (clients) {
                clients.add(out);
            }
            new Thread(new ClientHandler(socket, out)).start();
        }
    }

    static void broadcast(String message, PrintWriter sender) {
        synchronized (clients) {
            for (PrintWriter client : clients) {
                client.println(message);
            }
        }
    }

    static void removeClient(PrintWriter out) {
        synchronized (clients) {
            clients.remove(out);
        }
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private PrintWriter out;
    private String username;

    public ClientHandler(Socket socket, PrintWriter out) {
        this.socket = socket;
        this.out = out;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            username = in.readLine();
            ChatServer.broadcast("** " + username + " has joined the chat **", out);

            String message;
            while ((message = in.readLine()) != null) {
                ChatServer.broadcast(username + ": " + message, out);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected.");
        } finally {
            ChatServer.broadcast("** " + username + " has left the chat **", out);
            ChatServer.removeClient(out);
            try { socket.close(); } catch (IOException e) {}
        }
    }
}