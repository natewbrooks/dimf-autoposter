import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

public class Main {
    static DefaultListModel<String> previousPostsModel = new DefaultListModel<>();
    static JPanel imagePreviewPanel = new JPanel();

    public static void main(String[] args) {
        JFrame frame = new JFrame("DIMF Auto Poster");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        // SIDEBAR (left)
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(40, 40, 40));
        sidebar.setLayout(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(250, 0));

        JLabel sidebarTitle = new JLabel("Previous Posts");
        sidebarTitle.setForeground(Color.WHITE);
        sidebarTitle.setHorizontalAlignment(SwingConstants.CENTER);

        JList<String> postList = new JList<>(previousPostsModel);
        postList.setBackground(new Color(60, 60, 60));
        postList.setForeground(Color.WHITE);
        JScrollPane listScroll = new JScrollPane(postList);

        JButton downloadExcelButton = new JButton("Download Excel");

        sidebar.add(sidebarTitle, BorderLayout.NORTH);
        sidebar.add(listScroll, BorderLayout.CENTER);
        sidebar.add(downloadExcelButton, BorderLayout.SOUTH);
        frame.add(sidebar, BorderLayout.WEST);

        // MAIN PANEL (center)
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JTextField nameField = new JTextField();
        nameField.setBorder(BorderFactory.createTitledBorder("Name of Deceased"));

        JTextField dodField = new JTextField("YYYY-MM-DD");
        dodField.setBorder(BorderFactory.createTitledBorder("Date of Death"));

        JTextArea descriptionField = new JTextArea(3, 20);
        descriptionField.setBorder(BorderFactory.createTitledBorder("Description"));
        descriptionField.setLineWrap(true);

        JTextArea aiContentArea = new JTextArea(1, 20);
        aiContentArea.setBorder(BorderFactory.createTitledBorder("AI Content"));
        aiContentArea.setLineWrap(true);

        JCheckBox linkedin = new JCheckBox("LinkedIn");
        JCheckBox youtube = new JCheckBox("YouTube");
        JCheckBox twitter = new JCheckBox("X (Twitter)");
        JCheckBox facebook = new JCheckBox("Facebook");
        JCheckBox instagram = new JCheckBox("Instagram");

        JPanel platformPanel = new JPanel();
        platformPanel.setLayout(new GridLayout(0, 1));
        platformPanel.setBorder(BorderFactory.createTitledBorder("Platforms"));
        platformPanel.add(linkedin);
        platformPanel.add(youtube);
        platformPanel.add(twitter);
        platformPanel.add(facebook);
        platformPanel.add(instagram);

        // Image upload
        JButton uploadButton = new JButton("Upload Image");
        imagePreviewPanel.setLayout(new FlowLayout());
        imagePreviewPanel.setBorder(BorderFactory.createTitledBorder("Image Previews"));

        uploadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select Image(s)");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setMultiSelectionEnabled(true);
            fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif"));

            // Open in user's home directory
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

                // TODO: Save image paths or upload to backend
            }
        });

        JButton submitButton = new JButton("Generate & Post");

        // Add components
        panel.add(nameField);
        panel.add(dodField);
        panel.add(new JScrollPane(descriptionField));
        panel.add(new JScrollPane(aiContentArea));
        panel.add(platformPanel);
        panel.add(uploadButton);
        panel.add(imagePreviewPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(submitButton);
        frame.add(panel, BorderLayout.CENTER);

        // Dummy populate previous posts
        previousPostsModel.addElement("John Doe – 2024-11-02");
        previousPostsModel.addElement("Jane Smith – 2023-06-10");

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Download Excel Action
        downloadExcelButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "TODO: Export Excel logic here.");
        });
    }
}
