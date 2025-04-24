package components.sections;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import components.layout.WrapLayout;

public class ImageUploadPanel extends JPanel {
    private JPanel imagePreviewPanel;
    private JButton uploadButton;
    private Map<String, Component> imageComponents = new HashMap<>();
    private JLabel placeholderLabel;

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

        // Upload button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        uploadButton = new JButton("Upload Image");
        buttonPanel.add(uploadButton);

        add(previewScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        uploadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Image(s)");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif"));
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

            int option = fileChooser.showOpenDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                removePlaceholder();
                File[] selectedFiles = fileChooser.getSelectedFiles();
                for (File file : selectedFiles) {
                    addImage(file.getAbsolutePath());
                }
            }
        });

        addPlaceholder();
    }

    private void addPlaceholder() {
        if (placeholderLabel == null) {
            placeholderLabel = new JLabel("No images uploaded yet");
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

    private void addImage(String path) {
        if (imageComponents.containsKey(path)) return;
        removePlaceholder();

        // Create the image card
        JPanel card = new JPanel(new BorderLayout());
        card.setPreferredSize(new Dimension(110, 130));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Load and scale the image
        ImageIcon icon = new ImageIcon(path);
        Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        JLabel imgLabel = new JLabel(new ImageIcon(scaled));
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(imgLabel, BorderLayout.CENTER);

        // Create delete button
        JButton deleteBtn = new JButton("×");
        deleteBtn.setMargin(new Insets(0, 0, 0, 0));
        deleteBtn.setFont(new Font("Arial", Font.BOLD, 12));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setBackground(Color.RED);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setPreferredSize(new Dimension(20, 20));
        deleteBtn.addActionListener(e -> {
            imagePreviewPanel.remove(card);
            imageComponents.remove(path);
            if (imageComponents.isEmpty()) addPlaceholder();
            imagePreviewPanel.revalidate();
            imagePreviewPanel.repaint();
        });

        // Small top bar for the button
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        topBar.setOpaque(false);
        topBar.add(deleteBtn);
        card.add(topBar, BorderLayout.NORTH);

        // Add to grid
        imageComponents.put(path, card);
        imagePreviewPanel.add(card);
        imagePreviewPanel.revalidate();
        imagePreviewPanel.repaint();
    }


    public List<String> getUploadedImagePaths() {
        return new ArrayList<>(imageComponents.keySet());
    }

    public void loadImagesFromPaths(List<String> imagePaths) {
        clearImages();
        if (imagePaths != null && !imagePaths.isEmpty()) {
            removePlaceholder();
            for (String path : imagePaths) {
                addImage(path);
            }
        } else {
            addPlaceholder();
        }
    }

    public void clearImages() {
        imagePreviewPanel.removeAll();
        imageComponents.clear();
        placeholderLabel = null;
        addPlaceholder();
        imagePreviewPanel.revalidate();
        imagePreviewPanel.repaint();
    }
}
