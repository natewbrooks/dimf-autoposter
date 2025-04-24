import components.auth.LoginDialog;
import components.sections.*;
import lib.PostService;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame("DIMF Auto Poster");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);

        // Login first
        LoginDialog loginDialog = new LoginDialog(frame);
        loginDialog.setVisible(true);
        if (!loginDialog.isSucceeded()) {
            System.exit(0);
        }
        String username = loginDialog.getUsername();

        // Sidebar (WEST)
        DefaultListModel<String> previousPostsModel = new DefaultListModel<>();
        JButton downloadExcelButton = new JButton("Download Excel");

        // Create form panel with platform selector and image uploader
        FormPanel formPanel = new FormPanel();
        formPanel.setSidebarModel(previousPostsModel);

        // Create sidebar with reference to form panel
        SidebarPanel sidebar = new SidebarPanel(previousPostsModel, downloadExcelButton);
        sidebar.setFormPanel(formPanel); // Connect sidebar to form panel

        frame.add(sidebar, BorderLayout.WEST);

        // Top user header panel (not the full NORTH)
        JPanel userHeader = new UserHeaderPanel(username);

        // Main content panel
        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.add(formPanel);

        // Combine user header and main content vertically
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(userHeader, BorderLayout.NORTH);
        rightPanel.add(mainContentPanel, BorderLayout.CENTER);

        // Add combined right side to CENTER
        frame.add(rightPanel, BorderLayout.CENTER);

        // Load posts - using PostService instead of PostFetcher
        PostService.loadPosts(previousPostsModel);

        downloadExcelButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(frame, "TODO: Export Excel logic here.");
        });

        frame.setVisible(true);
    }
}