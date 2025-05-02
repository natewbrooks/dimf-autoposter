import components.auth.LoginDialog;
import components.sections.*;
import lib.PostService;
import lib.UserService;
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::initializeApplication);
    }
    
    private static void initializeApplication() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Create the main frame but don't show it yet
        JFrame frame = new JFrame("DIMF Auto Poster");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setLayout(new BorderLayout());
        frame.setLocationRelativeTo(null);
        
        // Show login dialog first
        authenticateUser(frame);
    }
    
    private static void authenticateUser(JFrame frame) {
        LoginDialog loginDialog = new LoginDialog(frame);
        loginDialog.setVisible(true);
        
        // Exit if login was canceled
        if (!loginDialog.isSucceeded()) {
            System.exit(0);
        }
        
        // If we get here, user is authenticated
        // Now initialize and show the main application
        initializeMainApp(frame);
    }
    
    private static void initializeMainApp(JFrame frame) {
        // Ensure user is logged in
        if (!UserService.isLoggedIn()) {
            System.out.println("Error: User should be logged in at this point");
            System.exit(1);
        }
        
        String username = UserService.getCurrentUsername();
        
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
        
        // Load posts - using PostService
        PostService.loadPosts(previousPostsModel);
        
        frame.setVisible(true);
    }
}