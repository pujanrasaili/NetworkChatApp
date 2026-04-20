package chatgui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatClientGUI extends JFrame {

    private static final String SERVER_IP = "localhost";
    private static final int PORT = 6000;

    private static final Color BG_DARK      = new Color(15, 15, 19);
    private static final Color BG_CARD      = new Color(22, 22, 29);
    private static final Color BG_INPUT     = new Color(30, 30, 46);
    private static final Color BG_BUBBLE_ME = new Color(109, 40, 217);
    private static final Color PURPLE       = new Color(124, 58, 237);
    private static final Color TEXT_WHITE   = new Color(226, 232, 240);
    private static final Color TEXT_MUTED   = new Color(255, 255, 255, 80);
    private static final Color BORDER       = new Color(255, 255, 255, 18);
    private static final Color GREEN_DOT    = new Color(34, 197, 94);

    private JPanel messagesPanel;
    private JScrollPane scrollPane;
    private JTextField inputField;
    private JPanel sidebarUsersPanel;
    private JLabel typingLabel;
    private PrintWriter out;
    private String username;
    private String avatarInitial;

    public ChatClientGUI() {
        username = JOptionPane.showInputDialog(null,
                "Enter your username:", "Welcome to NexChat",
                JOptionPane.PLAIN_MESSAGE);
        if (username == null || username.trim().isEmpty())
            username = "User" + (int)(Math.random() * 999);
        avatarInitial = String.valueOf(username.charAt(0)).toUpperCase();
        buildUI();
        connectToServer();
    }

    private void buildUI() {
        setTitle("NexChat — " + username);
        setSize(900, 620);
        setMinimumSize(new Dimension(700, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_DARK);
        add(buildSidebar(), BorderLayout.WEST);
        add(buildMainArea(), BorderLayout.CENTER);
        setVisible(true);
        inputField.requestFocus();
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(BG_CARD);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, BORDER));

        JPanel logoPanel = new JPanel(new BorderLayout());
        logoPanel.setBackground(BG_CARD);
        logoPanel.setBorder(new EmptyBorder(20, 16, 14, 16));
        logoPanel.setMaximumSize(new Dimension(220, 70));

        JLabel logo = new JLabel("◈ NexChat.");
        logo.setFont(new Font("SansSerif", Font.BOLD, 16));
        logo.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Connecting people");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        sub.setForeground(TEXT_MUTED);

        JPanel logoText = new JPanel();
        logoText.setLayout(new BoxLayout(logoText, BoxLayout.Y_AXIS));
        logoText.setBackground(BG_CARD);
        logoText.add(logo);
        logoText.add(Box.createVerticalStrut(2));
        logoText.add(sub);
        logoPanel.add(logoText, BorderLayout.CENTER);

        JSeparator sep1 = new JSeparator();
        sep1.setForeground(BORDER);
        sep1.setMaximumSize(new Dimension(220, 1));

        JLabel sectionLabel = new JLabel("ONLINE");
        sectionLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        sectionLabel.setForeground(TEXT_MUTED);
        sectionLabel.setBorder(new EmptyBorder(12, 16, 6, 16));
        sectionLabel.setAlignmentX(LEFT_ALIGNMENT);

        sidebarUsersPanel = new JPanel();
        sidebarUsersPanel.setLayout(new BoxLayout(sidebarUsersPanel, BoxLayout.Y_AXIS));
        sidebarUsersPanel.setBackground(BG_CARD);

        addUserToSidebar(sidebarUsersPanel, avatarInitial, username, "You", true);

        sidebar.add(logoPanel);
        sidebar.add(sep1);
        sidebar.add(sectionLabel);
        sidebar.add(sidebarUsersPanel);
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    private void addUserToSidebar(JPanel panel, String initial, String name, String status, boolean online) {
        // Don't add duplicates
        for (Component c : panel.getComponents()) {
            if (c instanceof JPanel) {
                JPanel row = (JPanel) c;
                if (row.getToolTipText() != null && row.getToolTipText().equals(name)) return;
            }
        }

        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG_CARD);
        row.setBorder(new EmptyBorder(7, 16, 7, 16));
        row.setMaximumSize(new Dimension(220, 50));
        row.setToolTipText(name); // used for duplicate check

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setBackground(BG_CARD);

        JLabel avatar = new JLabel(initial, SwingConstants.CENTER);
        avatar.setFont(new Font("SansSerif", Font.BOLD, 12));
        avatar.setForeground(Color.WHITE);
        avatar.setOpaque(true);
        avatar.setBackground(PURPLE);
        avatar.setPreferredSize(new Dimension(32, 32));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(BG_CARD);

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        nameLabel.setForeground(new Color(255, 255, 255, 200));

        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(TEXT_MUTED);

        textPanel.add(nameLabel);
        textPanel.add(statusLabel);

        JLabel dot = new JLabel("●");
        dot.setFont(new Font("SansSerif", Font.PLAIN, 10));
        dot.setForeground(online ? GREEN_DOT : new Color(255, 255, 255, 50));

        left.add(avatar);
        left.add(textPanel);
        row.add(left, BorderLayout.CENTER);
        row.add(dot, BorderLayout.EAST);
        panel.add(row);
    }

    private void removeUserFromSidebar(String name) {
        Component[] components = sidebarUsersPanel.getComponents();
        for (Component c : components) {
            if (c instanceof JPanel) {
                JPanel row = (JPanel) c;
                if (name.equals(row.getToolTipText())) {
                    sidebarUsersPanel.remove(row);
                    break;
                }
            }
        }
        sidebarUsersPanel.revalidate();
        sidebarUsersPanel.repaint();
    }

    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(BG_DARK);
        main.add(buildTopBar(), BorderLayout.NORTH);
        main.add(buildMessages(), BorderLayout.CENTER);
        main.add(buildInputArea(), BorderLayout.SOUTH);
        return main;
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(BG_CARD);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(12, 20, 12, 20)
        ));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setBackground(BG_CARD);

        JLabel hashLabel = new JLabel("#");
        hashLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        hashLabel.setForeground(PURPLE);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setBackground(BG_CARD);

        JLabel roomName = new JLabel("General Chat");
        roomName.setFont(new Font("SansSerif", Font.BOLD, 14));
        roomName.setForeground(Color.WHITE);

        typingLabel = new JLabel("All quiet here...");
        typingLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        typingLabel.setForeground(TEXT_MUTED);

        info.add(roomName);
        info.add(typingLabel);
        left.add(hashLabel);
        left.add(info);
        bar.add(left, BorderLayout.WEST);

        JLabel tagLabel = new JLabel("🔐 End-to-end encrypted");
        tagLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        tagLabel.setForeground(new Color(34, 197, 94, 150));
        bar.add(tagLabel, BorderLayout.EAST);

        return bar;
    }

    private JScrollPane buildMessages() {
        messagesPanel = new JPanel();
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBackground(BG_DARK);
        messagesPanel.setBorder(new EmptyBorder(16, 20, 8, 20));

        addSystemMessage("Welcome to NexChat · End-to-end encrypted");
        addSystemMessage("Logged in as " + username);

        scrollPane = new JScrollPane(messagesPanel);
        scrollPane.setBackground(BG_DARK);
        scrollPane.getViewport().setBackground(BG_DARK);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        return scrollPane;
    }

    private JPanel buildInputArea() {
        JPanel area = new JPanel(new BorderLayout());
        area.setBackground(BG_CARD);
        area.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(12, 20, 16, 20)
        ));

        JPanel inputWrap = new JPanel(new BorderLayout(10, 0));
        inputWrap.setBackground(BG_INPUT);
        inputWrap.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(6, 14, 6, 6)
        ));

        inputField = new JTextField();
        inputField.setBackground(BG_INPUT);
        inputField.setForeground(TEXT_WHITE);
        inputField.setCaretColor(new Color(167, 139, 250));
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        inputField.setBorder(BorderFactory.createEmptyBorder());
        inputField.putClientProperty("JTextField.placeholderText", "Message #general...");

        JButton sendBtn = new JButton("Send →") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PURPLE, getWidth(), getHeight(), BG_BUBBLE_ME);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        sendBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setPreferredSize(new Dimension(80, 36));
        sendBtn.setBorderPainted(false);
        sendBtn.setContentAreaFilled(false);
        sendBtn.setFocusPainted(false);
        sendBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendBtn.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (out != null) out.println("TYPING:" + username);
            }
        });

        inputWrap.add(inputField, BorderLayout.CENTER);
        inputWrap.add(sendBtn, BorderLayout.EAST);
        area.add(inputWrap, BorderLayout.CENTER);
        return area;
    }

    private void addSystemMessage(String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER));
        row.setBackground(BG_DARK);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lbl.setForeground(TEXT_MUTED);
        lbl.setBorder(new EmptyBorder(3, 12, 3, 12));
        lbl.setOpaque(true);
        lbl.setBackground(new Color(255, 255, 255, 15));

        row.add(lbl);
        messagesPanel.add(row);
        messagesPanel.add(Box.createVerticalStrut(8));
        messagesPanel.revalidate();
        messagesPanel.repaint();
    }

    private void addMessage(String senderName, String text, boolean isMe) {
        String time = new SimpleDateFormat("HH:mm").format(new Date());
        String initial = String.valueOf(senderName.charAt(0)).toUpperCase();

        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(BG_DARK);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        // Bubble panel
        JPanel bubble = new JPanel();
        bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
        bubble.setBackground(isMe ? BG_BUBBLE_ME : BG_INPUT);
        bubble.setBorder(new EmptyBorder(8, 12, 8, 12));

        if (!isMe) {
            JLabel nameLabel = new JLabel(senderName);
            nameLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
            nameLabel.setForeground(new Color(167, 139, 250));
            nameLabel.setAlignmentX(LEFT_ALIGNMENT);
            bubble.add(nameLabel);
            bubble.add(Box.createVerticalStrut(3));
        }

        // ── Fixed bubble size ──
        JLabel msgLabel = new JLabel("<html><body style='width:240px'>" +
                text.replace("&", "&amp;").replace("<", "&lt;") + "</body></html>");
        msgLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        msgLabel.setForeground(TEXT_WHITE);
        msgLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel timeLabel = new JLabel(time + (isMe ? " ✓✓" : ""));
        timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        timeLabel.setForeground(new Color(255, 255, 255, 80));
        timeLabel.setAlignmentX(isMe ? RIGHT_ALIGNMENT : LEFT_ALIGNMENT);

        bubble.add(msgLabel);
        bubble.add(Box.createVerticalStrut(4));
        bubble.add(timeLabel);

        // Avatar
        JLabel avatar = new JLabel(initial, SwingConstants.CENTER);
        avatar.setFont(new Font("SansSerif", Font.BOLD, 11));
        avatar.setForeground(Color.WHITE);
        avatar.setOpaque(true);
        avatar.setBackground(isMe ? PURPLE : new Color(5, 150, 105));
        avatar.setPreferredSize(new Dimension(28, 28));
        avatar.setMinimumSize(new Dimension(28, 28));
        avatar.setMaximumSize(new Dimension(28, 28));
        avatar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        JPanel avatarWrapper = new JPanel(new BorderLayout());
        avatarWrapper.setBackground(BG_DARK);
        avatarWrapper.setBorder(new EmptyBorder(2, 0, 0, 0));
        avatarWrapper.add(avatar, BorderLayout.NORTH);

        JPanel bubbleWrapper = new JPanel(new FlowLayout(
                isMe ? FlowLayout.RIGHT : FlowLayout.LEFT, 0, 0));
        bubbleWrapper.setBackground(BG_DARK);
        bubbleWrapper.add(bubble);

        if (isMe) {
            JPanel rightPanel = new JPanel(new BorderLayout(8, 0));
            rightPanel.setBackground(BG_DARK);
            rightPanel.add(bubbleWrapper, BorderLayout.CENTER);
            rightPanel.add(avatarWrapper, BorderLayout.EAST);
            row.add(rightPanel, BorderLayout.EAST);
        } else {
            JPanel leftPanel = new JPanel(new BorderLayout(8, 0));
            leftPanel.setBackground(BG_DARK);
            leftPanel.add(avatarWrapper, BorderLayout.WEST);
            leftPanel.add(bubbleWrapper, BorderLayout.CENTER);
            row.add(leftPanel, BorderLayout.WEST);
        }

        messagesPanel.add(row);
        messagesPanel.add(Box.createVerticalStrut(10));
        messagesPanel.revalidate();
        messagesPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = scrollPane.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket(SERVER_IP, PORT);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(username);

            new Thread(() -> {
                try {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        final String m = msg;
                        SwingUtilities.invokeLater(() -> handleIncoming(m));
                    }
                } catch (IOException e) {
                    SwingUtilities.invokeLater(() ->
                            addSystemMessage("Disconnected from server"));
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Cannot connect! Make sure Server.java is running.",
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleIncoming(String msg) {
        if (msg.startsWith("TYPING:")) {
            String typer = msg.substring(7);
            if (!typer.equals(username)) {
                typingLabel.setText(typer + " is typing...");
                Timer t = new Timer(2000, e -> typingLabel.setText("All quiet here..."));
                t.setRepeats(false);
                t.start();
            }
        } else if (msg.startsWith("JOIN:")) {
            String joiner = msg.substring(5);
            if (!joiner.equals(username)) {
                addSystemMessage("🟢 " + joiner + " joined");
            }
            addUserToSidebar(sidebarUsersPanel,
                    String.valueOf(joiner.charAt(0)).toUpperCase(),
                    joiner, "online", true);
            sidebarUsersPanel.revalidate();
            sidebarUsersPanel.repaint();
        } else if (msg.startsWith("LEAVE:")) {
            String leaver = msg.substring(6);
            addSystemMessage("🔴 " + leaver + " left");
            removeUserFromSidebar(leaver);
        } else if (msg.contains(": ")) {
            int idx = msg.indexOf(": ");
            String sender = msg.substring(0, idx);
            String text = msg.substring(idx + 2);
            addMessage(sender, text, sender.equals(username));
        } else {
            addSystemMessage(msg);
        }
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (!text.isEmpty() && out != null) {
            out.println(text);
            inputField.setText("");
        }
        inputField.requestFocus();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }
}