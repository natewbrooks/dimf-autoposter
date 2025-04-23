import javax.swing.*;
import java.awt.*;
import components.sections.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("DIMF Auto Poster");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLayout(new BorderLayout());

        DefaultListModel<String> previousPostsModel = new DefaultListModel<>();
        JButton downloadExcelButton = new JButton("Download Excel");
        SidebarPanel sidebar = new SidebarPanel(previousPostsModel, downloadExcelButton);
        frame.add(sidebar, BorderLayout.WEST);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        MainFormPanel formPanel = new MainFormPanel();
        PlatformSelectorPanel platformPanel = new PlatformSelectorPanel();
        ImageUploadPanel imageUploadPanel = new ImageUploadPanel();
        JButton submitButton = new JButton("Generate & Post");

        mainPanel.add(formPanel);
        mainPanel.add(platformPanel);
        mainPanel.add(imageUploadPanel);
        mainPanel.add(Box.createVerticalStrut(50));
        mainPanel.add(submitButton);

        frame.add(mainPanel, BorderLayout.CENTER);

        previousPostsModel.addElement("John Doe – 2024-11-02");
        previousPostsModel.addElement("Jane Smith – 2023-06-10");

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        downloadExcelButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "TODO: Export Excel logic here.");
        });
    }
}
