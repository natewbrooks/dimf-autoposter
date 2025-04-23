package components.sections;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class ImageUploadPanel extends JPanel {
    public JPanel imagePreviewPanel;
    public JButton uploadButton;

    public ImageUploadPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));

        imagePreviewPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        imagePreviewPanel.setBorder(BorderFactory.createTitledBorder("Image Previews"));
        imagePreviewPanel.setPreferredSize(new Dimension(500, 150));

        uploadButton = new JButton("Upload Image");
        JPanel buttonAlignRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonAlignRight.add(uploadButton);

        add(imagePreviewPanel);
        add(buttonAlignRight);

        uploadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Image(s)");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif"));
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

            int option = fileChooser.showOpenDialog(null);
            if (option == JFileChooser.APPROVE_OPTION) {
                File[] selectedFiles = fileChooser.getSelectedFiles();
                for (File file : selectedFiles) {
                    ImageIcon icon = new ImageIcon(file.getAbsolutePath());
                    Image scaled = icon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
                    JLabel imgLabel = new JLabel(new ImageIcon(scaled));
                    imagePreviewPanel.add(imgLabel);
                }
                imagePreviewPanel.revalidate();
                imagePreviewPanel.repaint();
            }
        });
    }
}