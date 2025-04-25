package components.auth;

import javax.swing.*;
import java.awt.*;
import lib.UserService;

public class RegisterDialog extends JDialog {
    private JTextField usernameField;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private boolean succeeded;

    public RegisterDialog(Frame parent) {
        super(parent, "Create Account", true);
        setSize(450, 300);
        setLayout(new GridBagLayout());
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Register New Account"),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // Username
        panel.add(new JLabel("Username:"));
        usernameField = new JTextField(20);
        panel.add(usernameField);

        // Email
        panel.add(new JLabel("Email:"));
        emailField = new JTextField(20);
        panel.add(emailField);

        // Password
        panel.add(new JLabel("Password:"));
        passwordField = new JPasswordField(20);
        panel.add(passwordField);

        // Confirm Password
        panel.add(new JLabel("Confirm Password:"));
        confirmPasswordField = new JPasswordField(20);
        panel.add(confirmPasswordField);

        // Buttons
        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");
        panel.add(registerButton);
        panel.add(cancelButton);

        // Register logic
        registerButton.addActionListener(e -> {
            String username = usernameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            // Validate form
            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please fill in all fields",
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this,
                        "Passwords do not match",
                        "Registration Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Register user
            UserService.register(username, email, password, result -> {
                if (result.success) {
                    JOptionPane.showMessageDialog(this,
                            "Registration successful!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    succeeded = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Registration failed: " + result.message,
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
        });

        // Cancel logic
        cancelButton.addActionListener(e -> {
            succeeded = false;
            dispose();
        });

        // ENTER key triggers register
        getRootPane().setDefaultButton(registerButton);

        add(panel, new GridBagConstraints());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}