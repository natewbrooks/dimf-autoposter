package components.sections;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;
import lib.PostService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FormPanel extends JPanel {
    // Core form fields
    private JTextField nameField;
    private DatePicker dodPicker;
    private JTextArea aiContentArea;
    private JButton generateButton;
    private JButton saveButton;
    private JPanel aiContainer;
    
    // Components that appear after content generation
    private PlatformSelectorPanel platformSelector;
    private ImageUploadPanel imageUploader;
    private JPanel additionalContentPanel;
    
    // State variables
    private PostService.PostData currentPostData = new PostService.PostData();
    private boolean isGenerating = false;
    private DefaultListModel<String> sidebarModel;
    
    public FormPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Create basic form fields
        createBasicFormFields();
        
        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        
        // Initialize platform selector and image uploader
        platformSelector = new PlatformSelectorPanel();
        imageUploader = new ImageUploadPanel();
        
        // Create container for additional content (platforms and images)
        additionalContentPanel = new JPanel();
        additionalContentPanel.setLayout(new BoxLayout(additionalContentPanel, BoxLayout.Y_AXIS));
        
        // Configure component layouts - simple full width setup
        platformSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
        imageUploader.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Add components to the additional content panel
        additionalContentPanel.add(platformSelector);
        additionalContentPanel.add(Box.createVerticalStrut(10)); // Spacing between components
        additionalContentPanel.add(imageUploader);
        
        // Initially hidden until content is generated
        additionalContentPanel.setVisible(false);
        
        // Add all components to main panel
//        add(nameField);
        add(Box.createVerticalStrut(10));
        add(dodPicker);
        add(Box.createVerticalStrut(10));
        add(buttonPanel);
        add(Box.createVerticalStrut(10));
        add(aiContainer);
        add(Box.createVerticalStrut(10));
        add(additionalContentPanel);
        add(Box.createVerticalStrut(10));
        add(saveButton);
    }
    
    private void createBasicFormFields() {
        // Create name field
    	nameField = new JTextField();
    	nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
    	nameField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Inner padding

    	// Wrap the name field in a panel with a titled border
    	JPanel nameContainer = new JPanel(new BorderLayout());
    	nameContainer.setBorder(BorderFactory.createTitledBorder("Name of Deceased"));
    	nameContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
    	nameContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
    	nameContainer.add(nameField, BorderLayout.CENTER);

    	// Assign to a field or just add directly to layout
    	this.nameField = nameField; // Keep for value access
    	this.add(nameContainer); // Add to FormPanel layout

        
        // Create date picker
        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
        dodPicker = new DatePicker(dateSettings);
        dodPicker.setBorder(BorderFactory.createTitledBorder("Date of Death"));
        dodPicker.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        dodPicker.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        aiContentArea = new JTextArea(15, 20);
        aiContentArea.setLineWrap(true);
        aiContentArea.setWrapStyleWord(true);
        aiContentArea.setEditable(true);
        aiContentArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Scrollable text area
        JScrollPane scrollPane = new JScrollPane(aiContentArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null); // Remove inner border so only the outer titled one shows

        // Outer panel with titled border (this mimics the original look)
        aiContainer = new JPanel(new BorderLayout());
        aiContainer.setBorder(BorderFactory.createTitledBorder("Post Content"));
        aiContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        aiContainer.setVisible(false);
        aiContainer.add(scrollPane, BorderLayout.CENTER);

    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        generateButton = new JButton("Generate Post");
        generateButton.addActionListener(this::handleGenerateButtonClick);
        buttonPanel.add(generateButton);
        
        saveButton = new JButton("Save Post");
        saveButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveButton.setVisible(false);
        saveButton.addActionListener(e -> savePost());
        
        return buttonPanel;
    }
    
    private void handleGenerateButtonClick(ActionEvent e) {
        if (isGenerating) {
            return; // Prevent multiple clicks
        }
        
        generateButton.setEnabled(false);
        generateButton.setText("Post Generating...");
        isGenerating = true;
        
        String name = nameField.getText().trim();
        String dod = (dodPicker.getDate() != null) 
            ? dodPicker.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) 
            : "";
        
        if (name.isEmpty() || dod.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both name and date of death.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            generateButton.setEnabled(true);
            generateButton.setText("Generate Post");
            isGenerating = false;
            return;
        }
        
        // Prepare UI for generating content
        aiContainer.setVisible(true);
        saveButton.setVisible(false);
        additionalContentPanel.setVisible(false);
        aiContentArea.setText("Generating Post...");
        aiContentArea.setEditable(false);
        
        // Use the service to generate content
        PostService.generatePostContent(name, dod, content -> {
            // Update UI with generated content
            aiContentArea.setText(content);
            aiContentArea.setEditable(true);
            
            // Show additional content panels
            additionalContentPanel.setVisible(true);
            saveButton.setVisible(true);
            
            // Update button state
            generateButton.setText("Regenerate Post");
            generateButton.setEnabled(true);
            isGenerating = false;
            
            // Update current post data
            currentPostData.name = name;
            currentPostData.dateOfDeath = dod;
            currentPostData.content = content;
            
            // Force layout update
            revalidate();
            repaint();
        });
    }

    public void setSidebarModel(DefaultListModel<String> model) {
        this.sidebarModel = model;
    }
    
    private void savePost() {
        // Get current form data
        currentPostData.name = nameField.getText().trim();
        currentPostData.dateOfDeath = (dodPicker.getDate() != null) 
            ? dodPicker.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) 
            : "";
        currentPostData.content = aiContentArea.getText();
        currentPostData.platformIds = platformSelector.getSelectedPlatformIds();
        currentPostData.imagePaths = imageUploader.getUploadedImagePaths();
        
        // Validate data
        if (currentPostData.name.isEmpty() || currentPostData.dateOfDeath.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both name and date of death.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Disable save button while saving
        saveButton.setEnabled(false);
        
        // Save using the service
        PostService.savePost(currentPostData, result -> {
            // Re-enable save button
            saveButton.setEnabled(true);
            
            if (result.success) {
                // Show success message
                JOptionPane.showMessageDialog(this, 
                    result.message, "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Update post ID if this was a new post
                if (currentPostData.postId < 0) {
                    currentPostData.postId = result.postId;
                }
                
                // Refresh sidebar
                if (sidebarModel != null) {
                    PostService.loadPosts(sidebarModel);
                }
                
                // Only reset form if it was a new post
                if (currentPostData.postId < 0) {
                    resetForm();
                }
            } else {
                // Show error message
                JOptionPane.showMessageDialog(this, 
                    result.message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    
    public void loadPostByName(String name) {
        // Show loading indicator
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        // Load post using the service
        PostService.loadPostByName(name, postData -> {
            // Reset cursor
            setCursor(Cursor.getDefaultCursor());
            
            if (postData == null) {
                JOptionPane.showMessageDialog(this, 
                    "Failed to load post: " + name, 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Update current post data
            currentPostData = postData;
            
            // Update UI
            nameField.setText(postData.name);
            
            try {
                // Parse the date
                LocalDate date = LocalDate.parse(postData.dateOfDeath.substring(0, 10));
                dodPicker.setDate(date);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Set content
            aiContentArea.setText(postData.content);
            aiContentArea.setCaretPosition(0);
            aiContainer.setVisible(true);
            
            // Show platforms and images
            additionalContentPanel.setVisible(true);
            
            // Set selected platforms
            platformSelector.setSelectedPlatforms(postData.platformIds);
            
            // Load images
            imageUploader.loadImagesFromPaths(postData.imagePaths);
            
            // Show save button
            saveButton.setVisible(true);
            
            // Update generate button text
            generateButton.setText("Generate New Content");
            generateButton.setEnabled(true);
            
            // Force layout update
            revalidate();
            repaint();
        });
    }
    
    public void resetForm() {
        SwingUtilities.invokeLater(() -> {
            // Clear form fields
            nameField.setText("");
            dodPicker.setDate(null);
            aiContentArea.setText("");
            
            // Reset platforms and images
            platformSelector.clearSelections();
            imageUploader.clearImages();
            
            // Reset UI state
            aiContainer.setVisible(false);
            additionalContentPanel.setVisible(false);
            saveButton.setVisible(false);
            generateButton.setText("Generate Post");
            generateButton.setEnabled(true);
            
            // Reset internal state variables
            currentPostData = new PostService.PostData();
            isGenerating = false;
        });
    }
}