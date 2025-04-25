package components.auth;

import javax.swing.*;
import java.awt.*;
import lib.UserService;

public class LoginDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private boolean succeeded;
    private String username;

    public LoginDialog(Frame parent) {
        super(parent, "Login", true);
        setSize(400, 250);
        setLayout(new GridBagLayout()); // Center content
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Log In"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // Username panel
        JPanel usernamePanel = new JPanel(new BorderLayout(5, 5));
        usernamePanel.add(new JLabel("Username:"), BorderLayout.WEST);
        usernameField = new JTextField("user", 20); // Placeholder
        usernamePanel.add(usernameField, BorderLayout.CENTER);
        panel.add(usernamePanel);

        // Password panel
        JPanel passwordPanel = new JPanel(new BorderLayout(5, 5));
        passwordPanel.add(new JLabel("Password:"), BorderLayout.WEST);
        passwordField = new JPasswordField("pass", 20); // Placeholder
        passwordPanel.add(passwordField, BorderLayout.CENTER);
        panel.add(passwordPanel);

        // Login button
        JButton loginButton = new JButton("Login");
        panel.add(loginButton);

        // Additional buttons panel
        JPanel extraButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton createAccountButton = new JButton("Create Account");
        JButton cancelButton = new JButton("Cancel");
        extraButtonsPanel.add(createAccountButton);
        extraButtonsPanel.add(cancelButton);
        panel.add(extraButtonsPanel);

        // Login logic
        loginButton.addActionListener(e -> {
            String enteredUsername = usernameField.getText();
            String enteredPassword = new String(passwordField.getPassword());

            // Use UserService to authenticate
            UserService.login(enteredUsername, enteredPassword, result -> {
                if (result.success) {
                    username = enteredUsername;
                    succeeded = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "Login failed: " + result.message,
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        });

        // Create Account logic
        createAccountButton.addActionListener(e -> {
            RegisterDialog registerDialog = new RegisterDialog(parent);
            registerDialog.setVisible(true);
            // If registration was successful, pre-fill the username field
            if (registerDialog.isSucceeded()) {
                // The username would be automatically filled if auto-login is enabled
                // after registration, but we'll still have this dialog open
                if (!UserService.isLoggedIn()) {
                    // Focus on password field since username might be pre-filled
                    passwordField.requestFocus();
                } else {
                    // Auto-login was successful, so we can close this dialog too
                    username = UserService.getCurrentUsername();
                    succeeded = true;
                    dispose();
                }
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

    public boolean isSucceeded() {
        return succeeded;
    }

    public String getUsername() {
        return username;
    }
}