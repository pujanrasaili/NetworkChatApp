package chatgui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ChatClientGUI extends JFrame {

    private static final String SERVER_IP = "localhost";
    private static final int PORT = 6000;

    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private PrintWriter out;
    private String username;

    public ChatClientGUI() {
        username = JOptionPane.showInputDialog(
                null,
                "Enter your username:",
                "Welcome to Chat",
                JOptionPane.PLAIN_MESSAGE
        );
        if (username == null || username.trim().isEmpty()) {
            username = "User" + (int)(Math.random() * 1000);
        }

        buildGUI();
        connectToServer();
    }

    private void buildGUI() {
        setTitle("💬 Chat App — " + username);
        setSize(550, 500);
        setMinimumSize(new Dimension(400, 350));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        // ── Top bar ──
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(41, 128, 185));
        topBar.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel titleLabel = new JLabel("💬 NetworkChatApp");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        JLabel userLabel = new JLabel("Logged in as: " + username);
        userLabel.setForeground(new Color(200, 230, 255));
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        topBar.add(titleLabel, BorderLayout.WEST);
        topBar.add(userLabel, BorderLayout.EAST);

        // ── Chat area ──
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        chatArea.setBackground(new Color(245, 245, 245));
        chatArea.setBorder(new EmptyBorder(10, 12, 10, 12));

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // ── Bottom input panel ──
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 0));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, new Color(220, 220, 220)),
                new EmptyBorder(10, 12, 10, 12)
        ));

        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        inputField.setBorder(new CompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));

        sendButton = new JButton("Send");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 13));
        sendButton.setBackground(new Color(41, 128, 185));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setBorder(new EmptyBorder(8, 20, 8, 20));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Hover effect on send button
        sendButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                sendButton.setBackground(new Color(31, 97, 141));
            }
            public void mouseExited(MouseEvent e) {
                sendButton.setBackground(new Color(41, 128, 185));
            }
        });

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

        // ── Assemble ──
        add(topBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setLocationRelativeTo(null); // center on screen
        setVisible(true);
        inputField.requestFocus();
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_IP, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(username);

            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream())
                    );
                    String message;
                    while ((message = in.readLine()) != null) {
                        final String msg = message;
                        SwingUtilities.invokeLater(() -> {
                            chatArea.append(msg + "\n");
                            chatArea.setCaretPosition(
                                    chatArea.getDocument().getLength()
                            );
                        });
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() ->
                            chatArea.append("⚠️ Disconnected from server.\n")
                    );
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "❌ Cannot connect to server!\nMake sure Server.java is running.",
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty() && out != null) {
            out.println(message);
            inputField.setText("");
        }
        inputField.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }
}