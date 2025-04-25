package components.sections;

import javax.swing.*;
import java.awt.*;

public class UserHeaderPanel extends JPanel {
    private JLabel usernameLabel;

    public UserHeaderPanel(String username) {
        setLayout(new BorderLayout()); // full-width panel
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)); // 8px left/right padding

        // Load and scale icon to 16x16
        ImageIcon originalIcon = new ImageIcon(getClass().getResource("/images/user-icon.png"));
        Image scaledImage = originalIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        // Create horizontal box to hold icon + username
        JPanel userBox = new JPanel();
        userBox.setLayout(new BoxLayout(userBox, BoxLayout.X_AXIS));
        userBox.setOpaque(false); // match background of parent
        userBox.add(new JLabel(scaledIcon));
        userBox.add(Box.createHorizontalStrut(6)); // small gap between icon and name

        usernameLabel = new JLabel(username);
        usernameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        userBox.add(usernameLabel);

        // Align to the right
        add(userBox, BorderLayout.EAST);
    }
}
