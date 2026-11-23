import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.*;
import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class server {
    static Color bg = new Color(18, 25, 43);
    static Color panelColor = new Color(29, 39, 60);
    static Color blue = new Color(65, 125, 255);
    static JPanel messages = new JPanel();
    static JTextField input = new JTextField();
    static JLabel status = new JLabel("● Starting...");
    static JScrollPane scroll;
    static Map<Integer, PrintWriter> clients =
        new ConcurrentHashMap<>();

    public static void main(String[] args) {
        JFrame frame = new JFrame("TCP Chat | Server");
        frame.setSize(500, 600);
        frame.setMinimumSize(new Dimension(400, 450));
        frame.setLocationByPlatform(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(bg);
        frame.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(panelColor);
        header.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("  TCP CHAT SERVER");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 19));

        status.setForeground(new Color(255, 200, 90));
        status.setFont(new Font("SansSerif", Font.PLAIN, 12));

        header.add(title, BorderLayout.NORTH);
        header.add(status, BorderLayout.SOUTH);
        frame.add(header, BorderLayout.NORTH);

        messages.setLayout(new BoxLayout(messages, BoxLayout.Y_AXIS));
        messages.setBackground(bg);
        messages.setBorder(new EmptyBorder(15, 12, 15, 12));

        scroll = new JScrollPane(messages);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(bg);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        frame.add(scroll, BorderLayout.CENTER);

        JButton send = new JButton("SEND ➤");
        send.setBackground(blue);
        send.setForeground(Color.WHITE);
        send.setFocusPainted(false);
        send.setFont(new Font("SansSerif", Font.BOLD, 12));

        input.setBackground(panelColor);
        input.setForeground(Color.WHITE);
        input.setCaretColor(Color.WHITE);
        input.setFont(new Font("SansSerif", Font.PLAIN, 14));
        input.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel bottom = new JPanel(new BorderLayout(10, 0));
        bottom.setBackground(bg);
        bottom.setBorder(new EmptyBorder(12, 12, 12, 12));
        bottom.add(input, BorderLayout.CENTER);
        bottom.add(send, BorderLayout.EAST);
        frame.add(bottom, BorderLayout.SOUTH);

        send.addActionListener(e -> sendMessage());
        input.addActionListener(e -> sendMessage());

        frame.setVisible(true);
        addMessage("SYSTEM", "Server started. Waiting for client...",
                   false, true);

       int[] ports = {4333, 4334, 4335, 4336, 4337, 4338};

for (int port : ports) {
    new Thread(() -> {
        try (ServerSocket server =
                new ServerSocket(port)) {

            setStatus("● Running | Listening...", Color.GREEN);

            addMessage("SYSTEM",
                    "Server listening on port " + port,
                    false, true);

            while (true) {
                Socket socket = server.accept();

                addMessage("SYSTEM",
                        "Client connected on port " + port,
                        false, true);

                new Thread(() -> handleClient(socket, port)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }).start();
}
    }
    //new
static void handleClient(Socket socket, int port) {

    try {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(socket.getInputStream())
        );

        PrintWriter writer = new PrintWriter(
            socket.getOutputStream(), true
        );

        clients.put(port, writer);

        setStatus("● Running | Clients: " +
                clients.size(), Color.GREEN);

        String message;

        while ((message = reader.readLine()) != null) {

            addMessage("CLIENT " + port,
        message, false, false);

// Forward message to other clients
for (Map.Entry<Integer, PrintWriter> entry :
        clients.entrySet()) {

    if (entry.getKey() != port) {
        entry.getValue().println(
            "CLIENT " + port + ": " + message
        );
    }
}
        }

    } catch (IOException e) {

        addMessage("SYSTEM",
                "Client disconnected: " + port,
                false, true);

    } finally {

        clients.remove(port);

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
    static void sendMessage() {

    String msg = input.getText().trim();

    if (!msg.isEmpty() && !clients.isEmpty()) {

        for (PrintWriter writer : clients.values()) {
            writer.println(msg);
        }

        addMessage("SERVER", msg, true, false);

        input.setText("");
    }
}

    static void setStatus(String text, Color color) {
        SwingUtilities.invokeLater(() -> {
            status.setText(text);
            status.setForeground(color);
        });
    }

    static void addMessage(String sender, String msg,
                           boolean mine, boolean system) {
        SwingUtilities.invokeLater(() -> {
            JPanel row = new JPanel(new FlowLayout(
                system ? FlowLayout.CENTER :
                mine ? FlowLayout.RIGHT : FlowLayout.LEFT));
            row.setBackground(bg);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel bubble = new JPanel();
            bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
            bubble.setBackground(system ? panelColor :
                mine ? new Color(44, 91, 190) :
                       new Color(39, 49, 70));
            bubble.setBorder(new EmptyBorder(9, 13, 9, 13));

            JLabel name = new JLabel(sender + "  •  " +
                LocalTime.now().format(
                    DateTimeFormatter.ofPattern("HH:mm")));
            name.setForeground(new Color(190, 205, 230));
            name.setFont(new Font("SansSerif", Font.BOLD, 10));

            JLabel body = new JLabel(
                "<html><div style='width:260px;'>" +
                escape(msg) + "</div></html>");
            body.setForeground(Color.WHITE);
            body.setFont(new Font("SansSerif", Font.PLAIN, 14));

            bubble.add(name);
            bubble.add(Box.createVerticalStrut(5));
            bubble.add(body);
            row.add(bubble);
            messages.add(row);
            messages.add(Box.createVerticalStrut(8));
            messages.revalidate();
            messages.repaint();
            scroll.getVerticalScrollBar().setValue(
                scroll.getVerticalScrollBar().getMaximum());
        });
    }

    static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;");
    }
} 