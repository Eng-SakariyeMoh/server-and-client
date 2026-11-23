
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.*;
import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class client3 {
    static Color bg = new Color(18, 25, 43);
    static Color panelColor = new Color(29, 39, 60);
    static Color blue = new Color(65, 125, 255);
    static JPanel messages = new JPanel();
    static JTextField input = new JTextField();
    static JLabel status = new JLabel("● Connecting...");
    static JScrollPane scroll;
    static volatile PrintWriter out;

    public static void main(String[] args) {
        JFrame frame = new JFrame("TCP Chat | Client");
        frame.setSize(500, 600);
        frame.setMinimumSize(new Dimension(400, 450));
        frame.setLocationByPlatform(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(bg);  
        frame.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(panelColor);
        header.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("  TCP CHAT CLIENT");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 19));

        status.setForeground(Color.ORANGE);
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
        addMessage("SYSTEM", "Connecting to server...",
                   false, true);

        new Thread(() -> {
            try (Socket socket = new Socket("172.16.181.49", 4336)) {
                out = new PrintWriter(
                    socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

                setStatus("● Connected", new Color(80, 220, 145));
                addMessage("SYSTEM", "Connected successfully!",
                           false, true);

                String msg;
                while ((msg = in.readLine()) != null) {
                    addMessage("SERVER", msg, false, false);
                }

                setStatus("● Disconnected", Color.ORANGE);
                addMessage("SYSTEM", "Server disconnected.",
                           false, true);

            } catch (Exception e) {
                setStatus("● Connection failed", Color.ORANGE);
                addMessage("SYSTEM",
                    "Cannot connect. Start the server first.",
                    false, true);
            }
        }).start();
    }

    static void sendMessage() {
        String msg = input.getText().trim();
        if (!msg.isEmpty() && out != null) {
            out.println(msg);
            addMessage("YOU", msg, true, false);
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
