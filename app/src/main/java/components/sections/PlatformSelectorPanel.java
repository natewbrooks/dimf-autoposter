package components.sections;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import components.SelectableImageButton;
import lib.PlatformService;

public class PlatformSelectorPanel extends JPanel {
    private Map<Integer, SelectableImageButton> platformButtons = new HashMap<>();
    private JPanel buttonContainer;
    private boolean isLoading = false;
    private JLabel loadingLabel;

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

        // Loading indicator
        loadingLabel = new JLabel("Loading platforms...");
        buttonContainer.add(loadingLabel);
        
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
        
        // Load platforms from the database
        loadPlatforms();
    }
    
    /**
     * Load platforms from the database
     */
    public void loadPlatforms() {
        if (isLoading) return;
        
        isLoading = true;
        buttonContainer.removeAll();
        buttonContainer.add(loadingLabel);
        buttonContainer.revalidate();
        buttonContainer.repaint();
        
        PlatformService.getAllPlatforms(result -> {
            isLoading = false;
            buttonContainer.removeAll();
            
            if (result.success && result.platforms != null) {
                for (PlatformService.PlatformData platform : result.platforms) {
                    addPlatformButton(platform.platformId, platform.iconUrl, platform.name);
                }
            } else {
                // Show error message if platforms couldn't be loaded
                JLabel errorLabel = new JLabel("Could not load platforms");
                errorLabel.setForeground(Color.RED);
                buttonContainer.add(errorLabel);
                
                // Log the error
                System.err.println("Failed to load platforms: " + 
                    (result.message != null ? result.message : "Unknown error"));
            }
            
            buttonContainer.revalidate();
            buttonContainer.repaint();
        });
    }

    /**
     * Add a platform button with an associated ID
     * @param id Platform ID
     * @param iconPath Path to platform icon
     * @param tooltip Tooltip text
     */
    private void addPlatformButton(int id, String iconPath, String tooltip) {
        // Create ImageIcon based on whether it's a resource path or a URL
        ImageIcon icon;
        try {
            if (iconPath.startsWith("/")) {
                // Resource path
                icon = new ImageIcon(getClass().getResource(iconPath));
            } else {
                // URL (for future use)
                icon = new ImageIcon(iconPath);
            }
            
            // Create the button with the icon
            SelectableImageButton button = new SelectableImageButton(icon);
            button.setToolTipText(tooltip);
            platformButtons.put(id, button);
            buttonContainer.add(button);
        } catch (Exception e) {
            // If icon can't be loaded, create a text button instead
            System.err.println("Failed to load icon for platform: " + tooltip + ", path: " + iconPath);
            JButton fallbackButton = new JButton(tooltip);
            fallbackButton.setForeground(Color.RED);
            buttonContainer.add(fallbackButton);
        }
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
    
    /**
     * Refresh the platform list from the database
     */
    public void refresh() {
        // Save currently selected IDs
        List<Integer> selectedIds = getSelectedPlatformIds();
        
        // Reload platforms
        loadPlatforms();
        
        // Restore selections after a short delay to ensure loading is complete
        Timer timer = new Timer(500, e -> setSelectedPlatforms(selectedIds));
        timer.setRepeats(false);
        timer.start();
    }
}