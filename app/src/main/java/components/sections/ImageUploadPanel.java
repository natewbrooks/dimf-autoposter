package components.sections;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import components.layout.WrapLayout;
import lib.ImageService;

public class ImageUploadPanel extends JPanel {
    private JPanel imagePreviewPanel;
    private JButton addImageButton;
    private Map<String, Component> imageComponents = new HashMap<>();
    private Map<String, Integer> imageIds = new HashMap<>(); // Map to track image IDs
    private JLabel placeholderLabel;
    private int currentPostId = -1; // Track current post ID

    public ImageUploadPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Image Previews"));

        // Panel sizing
        setMinimumSize(new Dimension(100, 150));
        setPreferredSize(new Dimension(100, 200));

        // Preview panel as wrapping grid
        imagePreviewPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 10));
        imagePreviewPanel.setBackground(Color.WHITE);

        // Scrollable wrapper
        JScrollPane previewScrollPane = new JScrollPane(imagePreviewPanel);
        previewScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        previewScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        previewScrollPane.setBorder(null);

        // Add image button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        addImageButton = new JButton("Add Image URL");
        addImageButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        buttonPanel.add(addImageButton);

        add(previewScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        addImageButton.addActionListener(e -> showAddImageDialog());

        addPlaceholder();
    }

    // Set the current post ID for image deletion
    public void setCurrentPostId(int postId) {
        this.currentPostId = postId;
    }

    private void showAddImageDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Image URL", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 140);
        dialog.setLocationRelativeTo(this);
        
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel urlLabel = new JLabel("Image URL:");
        JTextField urlField = new JTextField();
        
        JLabel sourceLabel = new JLabel("Source (optional):");
        JTextField sourceField = new JTextField();
        
        inputPanel.add(urlLabel);
        inputPanel.add(urlField);
        inputPanel.add(sourceLabel);
        inputPanel.add(sourceField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("Cancel");
        JButton addButton = new JButton("Add");
        
        cancelButton.addActionListener(e -> dialog.dispose());
        addButton.addActionListener(e -> {
            String url = urlField.getText().trim();
            if (!url.isEmpty()) {
                try {
                    // Validate URL by trying to create a URL object
                    new URL(url);
                    addImage(url, sourceField.getText().trim());
                    dialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Invalid URL format. Please enter a valid URL.", 
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter an image URL.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);
        
        dialog.add(inputPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addPlaceholder() {
        if (placeholderLabel == null) {
            placeholderLabel = new JLabel("No images added yet");
            placeholderLabel.setForeground(Color.GRAY);
            placeholderLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            imagePreviewPanel.add(placeholderLabel);
            imagePreviewPanel.revalidate();
            imagePreviewPanel.repaint();
        }
    }

    private void removePlaceholder() {
        if (placeholderLabel != null && placeholderLabel.getParent() == imagePreviewPanel) {
            imagePreviewPanel.remove(placeholderLabel);
            placeholderLabel = null;
            imagePreviewPanel.revalidate();
            imagePreviewPanel.repaint();
        }
    }

    private void addImage(String url, String source) {
        if (imageComponents.containsKey(url)) return;
        removePlaceholder();

        // Create the image card
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(110, 150));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        try {
            // Load and scale the image from URL
            Image image = ImageIO.read(new URL(url));
            if (image != null) {
                Image scaled = image.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                JLabel imgLabel = new JLabel(new ImageIcon(scaled));
                imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
                card.add(imgLabel, BorderLayout.CENTER);
            } else {
                // If image couldn't be loaded
                JLabel errorLabel = new JLabel("Image load error");
                errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
                card.add(errorLabel, BorderLayout.CENTER);
            }
        } catch (Exception e) {
            // If there was an error loading the image
            JLabel errorLabel = new JLabel("Image load error");
            errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
            card.add(errorLabel, BorderLayout.CENTER);
        }

        // URL label
        JLabel urlLabel = new JLabel("<html><div style='width:100px;text-align:center;'>" + url + "</div></html>");
        urlLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(urlLabel, BorderLayout.SOUTH);

        // Delete button
        JButton deleteBtn = new JButton("X");
        deleteBtn.setMargin(new Insets(0, 0, 0, 0));
        deleteBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setBackground(Color.RED);
        deleteBtn.setOpaque(true);
        deleteBtn.setContentAreaFilled(true);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteBtn.setPreferredSize(new Dimension(20, 20));

        deleteBtn.addActionListener(e -> {
            Integer imageId = imageIds.get(url);
            if (imageId != null && currentPostId > 0) {
                int confirm = JOptionPane.showConfirmDialog(
                    this, 
                    "Delete this image from the database?", 
                    "Confirm Deletion", 
                    JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    ImageService.deleteImage(currentPostId, imageId, result -> {
                        if (result.success) {
                            removeImageFromUI(url);
                        } else {
                            JOptionPane.showMessageDialog(
                                this,
                                "Failed to delete image: " + result.message,
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    });
                }
            } else {
                removeImageFromUI(url);
            }
        });

        // Small top bar for the button
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topBar.setOpaque(false);
        topBar.add(deleteBtn);
        card.add(topBar, BorderLayout.NORTH);

        // Add to grid
        imageComponents.put(url, card);
        imagePreviewPanel.add(card);
        imagePreviewPanel.revalidate();
        imagePreviewPanel.repaint();
    }

    // Helper method to remove image from UI
    private void removeImageFromUI(String url) {
        Component card = imageComponents.get(url);
        if (card != null) {
            imagePreviewPanel.remove(card);
            imageComponents.remove(url);
            imageIds.remove(url);
            if (imageComponents.isEmpty()) addPlaceholder();
            imagePreviewPanel.revalidate();
            imagePreviewPanel.repaint();
        }
    }

    public List<String> getImageUrls() {
        return new ArrayList<>(imageComponents.keySet());
    }

    // Updated to accept image data objects with IDs
    public void loadImagesFromData(List<ImageService.ImageData> images) {
        clearImages();
        if (images != null && !images.isEmpty()) {
            removePlaceholder();
            for (ImageService.ImageData image : images) {
                addImage(image.url, image.source != null ? image.source : "");
                // Store the image ID
                if (image.imageId > 0) {
                    imageIds.put(image.url, image.imageId);
                    System.out.println("DEBUG - ImageUploadPanel stored image ID: " + image.imageId + " for URL: " + image.url);
                }
            }
        } else {
            addPlaceholder();
        }
    }
    
    // Add this method to ImageUploadPanel class
    public void associateImageWithId(String url, int imageId) {
        if (url != null && !url.isEmpty() && imageId > 0) {
            System.out.println("DEBUG - ImageUploadPanel associating URL: " + url + " with ID: " + imageId);
            imageIds.put(url, imageId);
        }
    }

    // Maintain backward compatibility
    public void loadImagesFromUrls(List<String> imageUrls) {
        clearImages();
        if (imageUrls != null && !imageUrls.isEmpty()) {
            removePlaceholder();
            for (String url : imageUrls) {
                addImage(url, ""); // Source is not available when loading
            }
        } else {
            addPlaceholder();
        }
    }

    public void clearImages() {
        imagePreviewPanel.removeAll();
        imageComponents.clear();
        imageIds.clear();
        placeholderLabel = null;
        addPlaceholder();
        imagePreviewPanel.revalidate();
        imagePreviewPanel.repaint();
    }
}