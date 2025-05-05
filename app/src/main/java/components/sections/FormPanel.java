package components.sections;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.ArrayList;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import lib.ImageService;
import lib.PostService;

/**
 * Panel for creating and editing memorial posts
 */
public class FormPanel extends JPanel {
    // Constants
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Color DANGER_COLOR = new Color(220, 53, 69); // "danger" red
    
    // UI Components - Form Fields
    private JTextField nameField;
    private DatePicker dodPicker;
    private JTextArea aiContentArea;
    private JPanel aiContainer;
    
    // UI Components - Buttons
    private JButton generateButton;
    private JButton manualInputButton;
    private JButton saveButton;
    private JButton deleteButton;
    
    // UI Components - Sections
    private PlatformSelectorPanel platformSelector;
    private ImageUploadPanel imageUploader;
    private JPanel additionalContentPanel;
    private JPanel actionButtonsPanel;
    
    // State variables
    private PostService.PostData currentPostData = new PostService.PostData();
    private boolean isGenerating = false;
    private DefaultListModel<String> sidebarModel;
    private String currentSearchQuery = "";
    private List<Integer> previousPlatformIds = new ArrayList<>();
    
    /**
     * Constructs a new form panel
     */
    public FormPanel() {
        initializeLayout();
        createFormComponents();
        assembleComponents();
    }
    
    /**
     * Initialize the panel layout
     */
    private void initializeLayout() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    }
    
    /**
     * Create form UI components
     */
    private void createFormComponents() {
        createNameField();
        createDatePicker();
        createContentArea();
        createPlatformSelector();
        createImageUploader();
        createButtons();
        createActionButtonsPanel();
        createAdditionalContentPanel();
    }
    
    /**
     * Create name input field
     */
    private void createNameField() {
        nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        nameField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel nameContainer = new JPanel(new BorderLayout());
        nameContainer.setBorder(BorderFactory.createTitledBorder("Name of Deceased"));
        nameContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        nameContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameContainer.add(nameField, BorderLayout.CENTER);
        
        add(nameContainer);
    }
    
    /**
     * Create date picker
     */
    private void createDatePicker() {
        DatePickerSettings dateSettings = new DatePickerSettings();
        dateSettings.setFormatForDatesCommonEra("yyyy-MM-dd");
        dodPicker = new DatePicker(dateSettings);
        dodPicker.setBorder(BorderFactory.createTitledBorder("Date of Death"));
        dodPicker.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        dodPicker.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    
    /**
     * Create content text area
     */
    private void createContentArea() {
        aiContentArea = new JTextArea(15, 20);
        aiContentArea.setLineWrap(true);
        aiContentArea.setWrapStyleWord(true);
        aiContentArea.setEditable(true);
        aiContentArea.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        aiContentArea.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(aiContentArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);

        aiContainer = new JPanel(new BorderLayout());
        aiContainer.setBorder(BorderFactory.createTitledBorder("Post Content"));
        aiContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        aiContainer.setVisible(false);
        aiContainer.add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Create platform selector
     */
    private void createPlatformSelector() {
        platformSelector = new PlatformSelectorPanel();
        platformSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    
    /**
     * Create image uploader
     */
    private void createImageUploader() {
        imageUploader = new ImageUploadPanel();
        imageUploader.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    
    /**
     * Create buttons
     */
    private void createButtons() {
        // Generate button
        generateButton = new JButton("Generate Post");
        generateButton.addActionListener(this::handleGenerateButtonClick);
        generateButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Save button
        saveButton = new JButton("Save Post");
        saveButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveButton.setVisible(false);
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Pointer
        saveButton.addActionListener(e -> savePost());
        
        // Delete button
        deleteButton = new JButton("Delete Post");
        deleteButton.setBackground(DANGER_COLOR);
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setOpaque(true);
        deleteButton.setContentAreaFilled(true);
        deleteButton.setBorderPainted(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Pointer
        deleteButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        deleteButton.setVisible(false);
        deleteButton.addActionListener(e -> confirmDeletePost());

    }
    
    /**
     * Create panel for action buttons (save and delete)
     */
    private void createActionButtonsPanel() {
        actionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionButtonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionButtonsPanel.add(deleteButton);
        actionButtonsPanel.add(saveButton);
    }
    
    /**
     * Create panel for additional content
     */
    private void createAdditionalContentPanel() {
        additionalContentPanel = new JPanel();
        additionalContentPanel.setLayout(new BoxLayout(additionalContentPanel, BoxLayout.Y_AXIS));
        additionalContentPanel.add(platformSelector);
        additionalContentPanel.add(Box.createVerticalStrut(10));
        additionalContentPanel.add(imageUploader);
        additionalContentPanel.setVisible(false);
    }
    
    /**
     * Assemble all components into the panel
     */
    private void assembleComponents() {
        add(Box.createVerticalStrut(10));
        add(dodPicker);
        add(Box.createVerticalStrut(10));
        
        JPanel generateButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        generateButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        manualInputButton = new JButton("Manual Input");
        manualInputButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        manualInputButton.addActionListener(e -> {
            aiContainer.setVisible(true);
            additionalContentPanel.setVisible(true);
            saveButton.setVisible(true);
            deleteButton.setVisible(false);
            aiContentArea.setText("");
            aiContentArea.setEditable(true);
            manualInputButton.setVisible(false);  // hide itself after click
            revalidate();
            repaint();
        });

        generateButtonPanel.add(generateButton);
        generateButtonPanel.add(manualInputButton);
        add(generateButtonPanel);

        
        add(Box.createVerticalStrut(10));
        add(aiContainer);
        add(Box.createVerticalStrut(10));
        add(additionalContentPanel);
        add(Box.createVerticalStrut(10));
        add(actionButtonsPanel);
    }
    
    /**
     * Set the sidebar model for updates
     */
    public void setSidebarModel(DefaultListModel<String> model) {
        this.sidebarModel = model;
    }
    
    /**
     * Handle click on the generate button
     */
    private void handleGenerateButtonClick(ActionEvent e) {
        if (isGenerating) {
            return;
        }
        
        String name = nameField.getText().trim();
        String dod = getFormattedDate();
        
        if (!validateInputs(name, dod)) {
            return;
        }
        
        setGeneratingState(true);
        prepareUIForGenerating();
        
        // Create search query for Google
        String searchQuery = name + " " + dod;
        boolean needNewImageSearch = !searchQuery.equals(currentSearchQuery);
        currentSearchQuery = searchQuery;
        
        // Generate content
        generateContent(name, dod, needNewImageSearch);
    }
    
    /**
     * Get formatted date from picker
     */
    private String getFormattedDate() {
        return (dodPicker.getDate() != null) 
            ? dodPicker.getDate().format(DATE_FORMATTER) 
            : "";
    }
    
    /**
     * Validate form inputs
     */
    private boolean validateInputs(String name, String dod) {
        if (name.isEmpty() || dod.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both name and date of death.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            setGeneratingState(false);
            return false;
        }
        return true;
    }
    
    /**
     * Update UI state for generating
     */
    private void setGeneratingState(boolean generating) {
        isGenerating = generating;
        generateButton.setEnabled(!generating);
        generateButton.setText(generating ? "Post Generating..." : "Generate Post");
    }
    
    /**
     * Prepare UI components for content generation
     */
    private void prepareUIForGenerating() {
        aiContainer.setVisible(true);
        saveButton.setVisible(false);
        deleteButton.setVisible(false);
        additionalContentPanel.setVisible(false);
        aiContentArea.setText("Generating Post...");
        aiContentArea.setEditable(false);
    }
    
    /**
     * Generate post content
     */
    private void generateContent(String name, String dod, boolean performImageSearch) {
        PostService.generatePostContent(name, dod, content -> {
            updateUIWithGeneratedContent(name, dod, content);
            
            if (performImageSearch) {
                performImageSearch(name + " " + dod);
            }
            
            setGeneratingState(false);
            generateButton.setText("Regenerate Post");
            
            // Force layout update
            revalidate();
            repaint();
        });
    }
    
    /**
     * Update UI with generated content
     */
    private void updateUIWithGeneratedContent(String name, String dod, String content) {
        aiContentArea.setText(content);
        aiContentArea.setEditable(true);
        
        additionalContentPanel.setVisible(true);
        saveButton.setVisible(true);
        
        // Show delete button only for existing posts
        deleteButton.setVisible(currentPostData.postId > 0);
        manualInputButton.setVisible(false);

        
        // Update current post data
        currentPostData.name = name;
        currentPostData.dateOfDeath = dod;
        currentPostData.content = content;
    }
    
    /**
     * Perform Google image search
     */
    private void performImageSearch(String query) {
        ImageService.searchGoogleImages(query, result -> {
            if (result.success && result.images != null && !result.images.isEmpty()) {
                List<String> imageUrls = new ArrayList<>();
                for (ImageService.ImageData image : result.images) {
                    imageUrls.add(image.url);
                }
                
                imageUploader.loadImagesFromUrls(imageUrls);
            } else {
                imageUploader.clearImages();
                System.out.println("DEBUG - Image search failed or returned no results: " + 
                    (result.message != null ? result.message : "No message"));
            }
        });
    }
    
    /**
     * Save the post
     */
    private void savePost() {
        updateCurrentPostDataFromForm();
        
        // Validate data
        if (!validatePostData()) {
            return;
        }
        
        // Disable buttons while saving
        setButtonsEnabled(false);
        saveButton.setText("Saving...");
        
        savePostToDatabase();
    }
    
    /**
     * Update current post data from form values
     */
    private void updateCurrentPostDataFromForm() {
        currentPostData.name = nameField.getText().trim();
        currentPostData.dateOfDeath = getFormattedDate();
        currentPostData.content = aiContentArea.getText();
        currentPostData.platformIds = platformSelector.getSelectedPlatformIds();
        currentPostData.imagePaths = imageUploader.getImageUrls();
        
        System.out.println("DEBUG - Before saving post - Image URLs: " + currentPostData.imagePaths);
        System.out.println("DEBUG - Before saving post - Post ID: " + currentPostData.postId);
    }
    
    /**
     * Validate post data
     */
    private boolean validatePostData() {
        if (currentPostData.name.isEmpty() || currentPostData.dateOfDeath.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both name and date of death.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }
    
    /**
     * Set buttons enabled/disabled state
     */
    private void setButtonsEnabled(boolean enabled) {
        saveButton.setEnabled(enabled);
        deleteButton.setEnabled(enabled);
        generateButton.setEnabled(enabled);
    }
    
    /**
     * Save post to database
     */
    private void savePostToDatabase() {
        PostService.savePost(currentPostData, postResult -> {
            if (!postResult.success) {
                handleSaveError(postResult.message);
                return;
            }
            
            // Get post ID from result
            int postId = postResult.postId;
            System.out.println("DEBUG - FormPanel received post ID from save result: " + postId);
            
            // Check if we have a valid post ID
            if (postId > 0) {
                // Update current post data
                currentPostData.postId = postId;
                
                // Set the post ID in the image uploader for future delete operations
                imageUploader.setCurrentPostId(postId);
                
                // Update platform selections
                updatePlatformsForPost(postId);
                
                // Show delete button (now we have a valid post ID)
                deleteButton.setVisible(true);
                
                // Images are already linked on the server side in the replace_post_images method
                // so we don't need to process images here
                finalizeSaveOperation(postResult);
            } else {
                // No valid post ID, but operation was successful
                System.out.println("DEBUG - Warning: Save was successful but no valid post ID returned");
                
                // Show a warning to the user
                JOptionPane.showMessageDialog(
                    FormPanel.this,
                    "Post saved successfully, but the system couldn't retrieve the post ID. Some features may not work correctly.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE
                );
                
                setButtonsEnabled(true);
                saveButton.setText("Save Post");
            }
        });
    }
    
    /**
     * Handle save error
     */
    private void handleSaveError(String message) {
        setButtonsEnabled(true);
        saveButton.setText("Save Post");
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Handle successful post save
     */
    private void handleSuccessfulSave(int postId, PostService.SaveResult postResult) {
        System.out.println("DEBUG - Post saved successfully - Post ID from result: " + postId);
        
        // Update current post data with the new post ID
        currentPostData.postId = postId;
        
        // Update the image uploader's post ID first
        imageUploader.setCurrentPostId(postId);
        
        // Now process images if there are any
        if (!currentPostData.imagePaths.isEmpty()) {
            processImages(postId, postResult);
        } else {
            // Update platform selections
            updatePlatformsForPost(postId);
            
            // Update delete button visibility (now we have a valid post ID)
            deleteButton.setVisible(true);
            
            finalizeSaveOperation(postResult);
        }
    }
    
    /**
     * Update platform selections for the post
     */
    private void updatePlatformsForPost(int postId) {
        // Set the post ID in the image uploader for future delete operations
        imageUploader.setCurrentPostId(postId);
        
        // Get selected platform IDs
        List<Integer> selectedPlatforms = platformSelector.getSelectedPlatformIds();
        
        // Find newly selected platforms (not in previous selection)
        List<Integer> newlySelectedPlatformIds = new ArrayList<>(selectedPlatforms);
        newlySelectedPlatformIds.removeAll(previousPlatformIds);
        
        System.out.println("DEBUG - Previously selected platforms: " + previousPlatformIds);
        System.out.println("DEBUG - Currently selected platforms: " + selectedPlatforms);
        System.out.println("DEBUG - Newly selected platforms: " + newlySelectedPlatformIds);
        
        // Store the current selection for future reference
        currentPostData.platformIds = selectedPlatforms;

        // Update platforms in the database
        PostService.updatePostPlatforms(postId, selectedPlatforms, success -> {
            if (!success) {
                System.out.println("DEBUG - Failed to update platform selections for post ID: " + postId);
            } else {
                System.out.println("DEBUG - Platform selections updated successfully for post ID: " + postId);
                
                // Open URLs for newly selected platforms if there are any
                if (!newlySelectedPlatformIds.isEmpty()) {
                    openPlatformUrls(newlySelectedPlatformIds, postId);
                }
            }
            
            // Update previous selection for next time
            previousPlatformIds = new ArrayList<>(selectedPlatforms);
        });
    }
    
    /**
     * Open URLs for newly selected platforms
     */
    private void openPlatformUrls(List<Integer> platformIds, int postId) {
        if (platformIds.isEmpty() || postId <= 0) {
            return;
        }
        
        System.out.println("DEBUG - Opening URLs for platforms: " + platformIds);
        
        // Get all available platforms first
        lib.PlatformService.getAllPlatforms(result -> {
            if (!result.success || result.platforms == null) {
                System.out.println("DEBUG - Failed to get platform data for URL opening");
                return;
            }
            
            // Filter platforms by newly selected IDs
            for (lib.PlatformService.PlatformData platform : result.platforms) {
                if (platformIds.contains(platform.platformId)) {
                    String platformUrl = platform.platformUrl;
                    
                    if (platformUrl != null && !platformUrl.isEmpty()) {
                        System.out.println("DEBUG - Found platform URL: " + platformUrl + " for platform: " + platform.name);
                        
                        try {
                            // Format post content for sharing (if needed)
                            String formattedContent = formatPostForSharing(currentPostData.content);
                            String encodedContent = java.net.URLEncoder.encode(formattedContent, "UTF-8");
                            String encodedName = java.net.URLEncoder.encode(currentPostData.name, "UTF-8");
                            
                            // Customize URL for different platforms
                            String urlToOpen = platformUrl;
                            
                            // Add post content to URL based on platform type
                            if (platform.name.equalsIgnoreCase("Facebook")) {
                                // Facebook expects a URL to share
                                urlToOpen = platformUrl + "http://localhost/posts/" + postId;
                            } else if (platform.name.equalsIgnoreCase("X") || 
                                    platform.name.equalsIgnoreCase("Twitter")) {
                                // Twitter/X expects text parameter
                                urlToOpen = platformUrl + "text=" + encodedContent;
                            } else if (platform.name.equalsIgnoreCase("LinkedIn")) {
                                // LinkedIn expects a URL and can include a title
                                urlToOpen = platformUrl + "http://localhost/posts/" + postId + 
                                            "&title=" + encodedName;
                            }
                            
                            // Open the URL
                            final String finalUrl = urlToOpen;
                            System.out.println("DEBUG - Opening URL: " + finalUrl);
                            
                            // Open in a separate thread to avoid UI freezing
                            new Thread(() -> {
                                try {
                                    lib.BrowserUtil.openUrl(finalUrl);
                                } catch (Exception e) {
                                    System.out.println("DEBUG - Error opening URL: " + e.getMessage());
                                }
                            }).start();
                        } catch (java.io.UnsupportedEncodingException e) {
                            System.out.println("DEBUG - Error encoding URL parameters: " + e.getMessage());
                        }
                    }
                }
            }
        });
    }
    
    /**
     * Format post content for sharing on social media
     */
    private String formatPostForSharing(String content) {
        if (content == null || content.isEmpty()) {
            return "";
        }
        
        // Truncate content if too long
        final int MAX_LENGTH = 280; // Twitter-like length
        
        if (content.length() <= MAX_LENGTH) {
            return content;
        }
        
        // Truncate at last sentence or space before max length
        int lastSentence = content.lastIndexOf(". ", MAX_LENGTH - 3);
        if (lastSentence > MAX_LENGTH / 2) {
            return content.substring(0, lastSentence + 1) + "...";
        }
        
        int lastSpace = content.lastIndexOf(" ", MAX_LENGTH - 3);
        if (lastSpace > 0) {
            return content.substring(0, lastSpace) + "...";
        }
        
        // Fallback to hard truncation
        return content.substring(0, MAX_LENGTH - 3) + "...";
    }
    
    /**
     * Finalize save operation when no images need processing
     */
    private void finalizeSaveOperation(PostService.SaveResult postResult) {
        System.out.println("DEBUG - No images to process");
        setButtonsEnabled(true);
        saveButton.setText("Save Post");
        
        // Show success message
        JOptionPane.showMessageDialog(this, 
            postResult.message, "Success", JOptionPane.INFORMATION_MESSAGE);
        
        refreshSidebarAndUI();
    }
    
    /**
     * Process images for the post
     */
    private void processImages(int postId, PostService.SaveResult postResult) {
        // Verify valid post ID
        if (postId <= 0) {
            handleInvalidPostId();
            return;
        }
        
        // Set up for image processing
        final int finalPostId = postId;
        final int[] imagesProcessed = {0};
        final int totalImages = currentPostData.imagePaths.size();
        final boolean[] hasErrors = {false};
        
        System.out.println("DEBUG - Starting to process " + totalImages + " images for post ID: " + finalPostId);
        
        // Process each image
        for (String imageUrl : currentPostData.imagePaths) {
            processImage(imageUrl, finalPostId, imagesProcessed, totalImages, hasErrors, postResult);
        }
    }
    
    
    
    /**
     * Handle invalid post ID
     */
    private void handleInvalidPostId() {
        System.out.println("DEBUG - Invalid post ID for image linking");
        setButtonsEnabled(true);
        saveButton.setText("Save Post");
        
        JOptionPane.showMessageDialog(this, 
            "Post saved but couldn't get a valid ID for image linking. Please try again.", 
            "Warning", JOptionPane.WARNING_MESSAGE);
            
        refreshSidebarAndUI();
    }
    
    /**
     * Process a single image
     */
    private void processImage(String imageUrl, int postId, int[] imagesProcessed, 
            int totalImages, boolean[] hasErrors, PostService.SaveResult postResult) {
        
        System.out.println("DEBUG - Processing image URL: " + imageUrl + " for post ID: " + postId);
        
        // First save the image
        ImageService.addImage(imageUrl, "", imageResult -> {
            if (imageResult.success && imageResult.imageId > 0) {
                // Then link it to the post
                int imageId = imageResult.imageId;
                // Ensure we're using the correct post ID
                linkImageToPost(imageId, postId, imagesProcessed, totalImages, hasErrors, postResult);
            } else {
                handleImageAddError(imageUrl, imageResult.message, imagesProcessed, totalImages, hasErrors, postResult);
            }
        });
    }
    
    /**
     * Link image to post
     */
    private void linkImageToPost(int imageId, int postId, int[] imagesProcessed, 
            int totalImages, boolean[] hasErrors, PostService.SaveResult postResult) {
        
        System.out.println("DEBUG - Image added successfully - Image ID: " + imageId + " for post ID: " + postId);
        
        ImageService.linkImageToPost(postId, imageId, linkResult -> {
            synchronized (imagesProcessed) {
                imagesProcessed[0]++;
                
                if (linkResult.success) {
                    System.out.println("DEBUG - Image linked successfully - Image ID: " + imageId + ", Post ID: " + postId);
                } else {
                    System.out.println("DEBUG - Failed to link image - Image ID: " + imageId + ", Post ID: " + postId);
                    System.out.println("DEBUG - Error: " + linkResult.message);
                    hasErrors[0] = true;
                }
                
                checkImageProcessingComplete(imagesProcessed[0], totalImages, hasErrors[0], postResult);
            }
        });
    }
    
    /**
     * Handle image add error
     */
    private void handleImageAddError(String imageUrl, String message, int[] imagesProcessed, 
            int totalImages, boolean[] hasErrors, PostService.SaveResult postResult) {
        
        System.out.println("DEBUG - Failed to add image: " + imageUrl);
        System.out.println("DEBUG - Error: " + message);
        
        synchronized (imagesProcessed) {
            imagesProcessed[0]++;
            hasErrors[0] = true;
            
            checkImageProcessingComplete(imagesProcessed[0], totalImages, hasErrors[0], postResult);
        }
    }
    
    /**
     * Check if image processing is complete
     */
    private void checkImageProcessingComplete(int processed, int total, boolean hasErrors, 
            PostService.SaveResult postResult) {
        
        if (processed >= total) {
            System.out.println("DEBUG - All images processed - Total: " + total + ", Errors: " + hasErrors);
            
            SwingUtilities.invokeLater(() -> {
                setButtonsEnabled(true);
                saveButton.setText("Save Post");
                
                // Show appropriate message
                if (hasErrors) {
                    JOptionPane.showMessageDialog(FormPanel.this, 
                        "Post saved but some images could not be processed.", 
                        "Partial Success", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(FormPanel.this, 
                        postResult.message, "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                
                refreshSidebarAndUI();
            });
        }
    }
    
    /**
     * Confirm post deletion with a dialog
     */
    private void confirmDeletePost() {
        if (currentPostData.postId <= 0) {
            JOptionPane.showMessageDialog(this,
                "Cannot delete a post that hasn't been saved yet.",
                "Delete Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Create a custom confirmation dialog with a red warning
        JPanel panel = new JPanel(new BorderLayout());
        
        // Add warning icon and message
//        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.warningIcon"));
//        panel.add(iconLabel, BorderLayout.WEST);
//        
        JPanel messagePanel = new JPanel(new BorderLayout());
        JLabel warningLabel = new JLabel("<html><b>Warning: This action cannot be undone!</b></html>");
        warningLabel.setForeground(DANGER_COLOR);
        
        JLabel messageLabel = new JLabel("<html>Are you sure you want to delete the post:<br/><b>" + 
            currentPostData.name + "</b>?</html>");
        
        messagePanel.add(warningLabel, BorderLayout.NORTH);
        messagePanel.add(messageLabel, BorderLayout.CENTER);
        panel.add(messagePanel, BorderLayout.CENTER);
        
        // Show the dialog
        int option = JOptionPane.showConfirmDialog(
            this,
            panel,
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            deletePost();
        }
    }
    
    /**
     * Delete the post
     */
    private void deletePost() {
        final int postId = currentPostData.postId;
        
        // Disable UI during deletion
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        setButtonsEnabled(false);
        deleteButton.setText("Deleting...");
        
        // Create a SwingWorker for the delete operation
        SwingWorker<Boolean, Void> deleteWorker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    // Create the DELETE request
                    URL deleteUrl = new URL(PostService.getApiBaseUrl() + "/posts/" + postId);
                    HttpURLConnection conn = (HttpURLConnection) deleteUrl.openConnection();
                    conn.setRequestMethod("DELETE");
                    
                    // Get the response code
                    int responseCode = conn.getResponseCode();
                    return responseCode >= 200 && responseCode < 300;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    
                    // Reset cursor and re-enable buttons
                    setCursor(Cursor.getDefaultCursor());
                    setButtonsEnabled(true);
                    deleteButton.setText("Delete Post");
                    
                    if (success) {
                        JOptionPane.showMessageDialog(FormPanel.this,
                            "Post deleted successfully.",
                            "Delete Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        // Reset form and refresh sidebar
                        resetForm();
                        if (sidebarModel != null) {
                            PostService.loadPosts(sidebarModel);
                        }
                    } else {
                        JOptionPane.showMessageDialog(FormPanel.this,
                            "Failed to delete the post. Please try again.",
                            "Delete Failed",
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    setCursor(Cursor.getDefaultCursor());
                    setButtonsEnabled(true);
                    deleteButton.setText("Delete Post");
                    
                    JOptionPane.showMessageDialog(FormPanel.this,
                        "An error occurred: " + e.getMessage(),
                        "Delete Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        deleteWorker.execute();
    }
    
    /**
     * Refresh sidebar and UI components
     */
    private void refreshSidebarAndUI() {
        if (sidebarModel != null) {
            PostService.loadPosts(sidebarModel);
            platformSelector.refresh();
        }
    }
    
    /**
     * Load a post by name
     */
    public void loadPostByName(String name) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        PostService.loadPostByName(name, postData -> {
            setCursor(Cursor.getDefaultCursor());
            
            if (postData == null) {
                JOptionPane.showMessageDialog(this, 
                    "Failed to load post: " + name, 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            loadPostDataIntoForm(postData);
        });
    }
    
    /**
     * Load post data into form
     */
    private void loadPostDataIntoForm(PostService.PostData postData) {
        // Update current post data
        currentPostData = postData;
        
        // Update form fields
        updateFormFields(postData);
        
        // Load related data
        loadPostRelatedData(postData);
        
        // Update UI visibility
        updateUIForLoadedPost();
        
        // Store current platforms as previous
        previousPlatformIds = new ArrayList<>(postData.platformIds);
    }
    
    /**
     * Update form fields with post data
     */
    private void updateFormFields(PostService.PostData postData) {
        nameField.setText(postData.name);
        
        try {
            // Parse the date
            LocalDate date = LocalDate.parse(postData.dateOfDeath.substring(0, 10));
            dodPicker.setDate(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        aiContentArea.setText(postData.content);
        aiContentArea.setCaretPosition(0);
    }
    
    /**
     * Load post-related data (platforms and images)
     */
    private void loadPostRelatedData(PostService.PostData postData) {
        if (postData.postId > 0) {
            final int postId = postData.postId;
            
            // Set the post ID in the image uploader
            imageUploader.setCurrentPostId(postId);
            
            // Load images from API
            loadImagesForPost(postId);
            
            // Load platforms for post
            loadPlatformsForPost(postId);
        } else {
            // For compatibility with older data format
            imageUploader.loadImagesFromUrls(postData.imagePaths);
            platformSelector.setSelectedPlatforms(postData.platformIds);
        }
    }
    
    /**
     * Load images for post
     */
    private void loadImagesForPost(int postId) {
        ImageService.getImagesForPost(postId, imageResult -> {
            if (imageResult.success && imageResult.images != null) {
                imageUploader.loadImagesFromData(imageResult.images);
            } else {
                imageUploader.clearImages();
            }
        });
    }
    
    /**
     * Load platforms for post
     */
    private void loadPlatformsForPost(int postId) {
        platformSelector.clearSelections();
        
        PostService.getPostPlatforms(postId, platformIds -> {
            platformSelector.setSelectedPlatforms(platformIds);
            currentPostData.platformIds = platformIds;
        });
    }
    
    /**
     * Update UI visibility for loaded post
     */
    private void updateUIForLoadedPost() {
        aiContainer.setVisible(true);
        additionalContentPanel.setVisible(true);
        saveButton.setVisible(true);
        
        // Only show delete button for existing posts
        deleteButton.setVisible(currentPostData.postId > 0);
        
        generateButton.setText("Generate New Content");
        generateButton.setEnabled(true);
        
        manualInputButton.setVisible(false);
        
        revalidate();
        repaint();
    }
    
    /**
     * Reset the form
     */
    public void resetForm() {
        SwingUtilities.invokeLater(() -> {
            // Clear form fields
            nameField.setText("");
            dodPicker.setDate(null);
            aiContentArea.setText("");
            
            // Reset platforms and images
            platformSelector.clearSelections();
            platformSelector.refresh();
            
            imageUploader.clearImages();
            imageUploader.setCurrentPostId(-1);
            
            // Reset UI state
            aiContainer.setVisible(false);
            additionalContentPanel.setVisible(false);
            saveButton.setVisible(false);
            deleteButton.setVisible(false);
            generateButton.setText("Generate Post");
            generateButton.setEnabled(true);
            
            manualInputButton.setVisible(true);
            
            // Reset internal state variables
            currentPostData = new PostService.PostData();
            isGenerating = false;
            
            // Reset platform tracking
            previousPlatformIds.clear();
        });
    }
}