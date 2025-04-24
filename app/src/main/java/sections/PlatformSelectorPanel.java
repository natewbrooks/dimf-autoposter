package sections;

import javax.swing.*;
import java.awt.*;
import components.SelectableImageButton;

public class PlatformSelectorPanel extends JPanel {
    public PlatformSelectorPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Platforms"));

        // Container for buttons
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        buttonContainer.add(new SelectableImageButton(new ImageIcon(getClass().getResource("/images/linkedin.png"))));
        buttonContainer.add(new SelectableImageButton(new ImageIcon(getClass().getResource("/images/youtube.png"))));
        buttonContainer.add(new SelectableImageButton(new ImageIcon(getClass().getResource("/images/x.png"))));
        buttonContainer.add(new SelectableImageButton(new ImageIcon(getClass().getResource("/images/facebook.png"))));
        buttonContainer.add(new SelectableImageButton(new ImageIcon(getClass().getResource("/images/instagram.png"))));

        // Scroll pane for overflow handling
        JScrollPane scrollPane = new JScrollPane(buttonContainer,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null); // Remove scroll pane border if not needed
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16); // Smoother scrolling

        add(scrollPane, BorderLayout.CENTER);
    }
}
