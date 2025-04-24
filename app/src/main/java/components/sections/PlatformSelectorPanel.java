package components.sections;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import components.SelectableImageButton;

public class PlatformSelectorPanel extends JPanel {
    private Map<Integer, SelectableImageButton> platformButtons = new HashMap<>();
    private JPanel buttonContainer;

    public PlatformSelectorPanel() {
    	setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Platforms"));

        // Set preferred height but allow full width expansion
        setMinimumSize(new Dimension(100, 60));
        setPreferredSize(new Dimension(Integer.MAX_VALUE, 60));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); // Limit height but not width

        // Container for buttons - use a more compact layout
        buttonContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 2));
        buttonContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));  // allow expansion


        // Add platform buttons with ID mapping
        addPlatformButton(1, "/images/linkedin.png", "LinkedIn");
        addPlatformButton(2, "/images/youtube.png", "YouTube");
        addPlatformButton(3, "/images/x.png", "X");
        addPlatformButton(4, "/images/facebook.png", "Facebook");
        addPlatformButton(5, "/images/instagram.png", "Instagram");

        // Scroll pane for overflow handling - this ensures buttons are always fully visible
        JScrollPane scrollPane = new JScrollPane(buttonContainer,
                JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null); // Remove scroll pane border
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16); // Smoother scrolling

        JPanel scrollWrapper = new JPanel(new BorderLayout());
        scrollWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        scrollWrapper.add(scrollPane, BorderLayout.CENTER);
        add(scrollWrapper);

    }

    /**
     * Add a platform button with an associated ID
     * @param id Platform ID
     * @param iconPath Path to platform icon
     * @param tooltip Tooltip text
     */
    private void addPlatformButton(int id, String iconPath, String tooltip) {
        SelectableImageButton button = new SelectableImageButton(
                new ImageIcon(getClass().getResource(iconPath)));
        button.setToolTipText(tooltip);
        platformButtons.put(id, button);
        buttonContainer.add(button);
    }

    /**
     * Get IDs of all currently selected platforms
     * @return List of selected platform IDs
     */
    public List<Integer> getSelectedPlatformIds() {
        List<Integer> selectedIds = new ArrayList<>();
        for (Map.Entry<Integer, SelectableImageButton> entry : platformButtons.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedIds.add(entry.getKey());
            }
        }
        return selectedIds;
    }

    /**
     * Set selected platforms based on a list of platform IDs
     * @param platformIds List of platform IDs to select
     */
    public void setSelectedPlatforms(List<Integer> platformIds) {
        // First clear all selections
        clearSelections();

        // Then set the specified ones
        if (platformIds != null) {
            for (Integer id : platformIds) {
                SelectableImageButton button = platformButtons.get(id);
                if (button != null) {
                    button.setSelected(true);
                }
            }
        }
    }

    /**
     * Clear all platform selections
     */
    public void clearSelections() {
        for (SelectableImageButton button : platformButtons.values()) {
            button.setSelected(false);
        }
    }
}