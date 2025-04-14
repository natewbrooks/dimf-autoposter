import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Main {
    public static void main(String[] args) {
        // Load environment variables for DB
        String host = System.getenv("DB_HOST");
        String port = System.getenv("DB_PORT");
        String dbName = System.getenv("DB_NAME");
        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASS");
        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;

        // Build UI
        JFrame frame = new JFrame("DIMF Auto Poster");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        JLabel titleLabel = new JLabel("More like auto poser haha", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

        JTextField inputField = new JTextField();
        inputField.setMaximumSize(new Dimension(300, 30));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(Box.createVerticalStrut(30));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(20));
        panel.add(inputField);

        frame.add(panel);
        frame.setLocationRelativeTo(null); // Center the window
        frame.setVisible(true);
    }
}
