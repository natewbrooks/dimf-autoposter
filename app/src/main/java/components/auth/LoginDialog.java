package components.auth;

import javax.swing.*;
import java.awt.*;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private boolean succeeded;
    private String username;

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        setSize(900, 600);
        setLayout(new GridBagLayout()); // Center content
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
        	    BorderFactory.createTitledBorder("Log In"),
        	    BorderFactory.createEmptyBorder(8, 8, 8, 8)
        	));
        
        // Username
        panel.add(new JLabel("Username:"));
        usernameField = new JTextField("user", 20); // Placeholder
        panel.add(usernameField);

        // Password
        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField("pass", 20); // Placeholder
        panel.add(passwordField);

        // Buttons
        JButton loginButton = new JButton("Login");
        JButton cancelButton = new JButton("Cancel");
        panel.add(loginButton);
        panel.add(cancelButton);

        // Login logic
        loginButton.addActionListener(e -> {
            String enteredUsername = usernameField.getText();
            String enteredPassword = new String(passwordField.getPassword());

            if (authenticate(enteredUsername, enteredPassword)) {
                username = enteredUsername;
                succeeded = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(LoginDialog.this,
                        "Invalid credentials",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Cancel logic
        cancelButton.addActionListener(e -> {
            succeeded = false;
            dispose();
        });

        // ENTER key triggers login
        getRootPane().setDefaultButton(loginButton);

        add(panel, new GridBagConstraints());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private boolean authenticate(String username, String password) {
        try {
            URL url = new URL("http://localhost:8000/api/auth/login");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String json = String.format("{\"username\":\"%s\", \"password\":\"%s\"}", username, password);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes());
                os.flush();
            }

            return conn.getResponseCode() == 200;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public String getUsername() {
        return username;
    }
}
