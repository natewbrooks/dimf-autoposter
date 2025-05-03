package components.sections;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class UserHeaderPanel extends JPanel {
    private JLabel usernameLabel;

    public UserHeaderPanel(String username) {
        setLayout(new BorderLayout()); // full-width panel
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8)); // 8px left/right padding

        // Load and scale icon to 16x16
        ImageIcon scaledIcon = null;

        try (InputStream is = getClass().getResourceAsStream("/resources/images/user-icon.png")) {
            if (is != null) {
                Image image = ImageIO.read(is);
                Image scaledImage = image.getScaledInstance(16, 16, Image.SCALE_SMOOTH);
                scaledIcon = new ImageIcon(scaledImage);
            } else {
                System.err.println("WARNING: /resources/images/user-icon.png not found in JAR.");
            }
        } catch (IOException e) {
            System.err.println("ERROR loading /resources/images/user-icon.png: " + e.getMessage());
        }

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
