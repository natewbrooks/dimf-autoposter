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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.DatePickerSettings;

import lib.PostService;

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
    
    private String currentSearchQuery = ""; // Track the current search query
    
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
        
        // Create search query for Google
        String searchQuery = name + " " + dod;
        
        // Check if we need to perform a new image search
        boolean needNewImageSearch = !searchQuery.equals(currentSearchQuery);
        currentSearchQuery = searchQuery;
        
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
            
            // Perform Google Images search if needed
            if (needNewImageSearch) {
                performImageSearch(searchQuery);
            }
            
            // Force layout update
            revalidate();
            repaint();
        });
    }

    // Add this new method to the FormPanel class
    private void performImageSearch(String query) {
        lib.ImageService.searchGoogleImages(query, result -> {
            if (result.success && result.images != null && !result.images.isEmpty()) {
                // Get the URLs from the images
                List<String> imageUrls = new ArrayList<>();
                for (lib.ImageService.ImageData image : result.images) {
                    imageUrls.add(image.url);
                }
                
                // Load the images into the image uploader
                imageUploader.loadImagesFromUrls(imageUrls);
            } else {
                // Clear any existing images if the search failed
                imageUploader.clearImages();
                System.out.println("DEBUG - Image search failed or returned no results: " + 
                    (result.message != null ? result.message : "No message"));
            }
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
        
        // Get image URLs and store them in currentPostData
        currentPostData.imagePaths = imageUploader.getImageUrls();
        
        // DEBUG: Print image paths before saving
        System.out.println("DEBUG - Before saving post - Image URLs: " + currentPostData.imagePaths);
        System.out.println("DEBUG - Before saving post - Post ID: " + currentPostData.postId);
        
        // Validate data
        if (currentPostData.name.isEmpty() || currentPostData.dateOfDeath.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter both name and date of death.", 
                "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Disable save button while saving
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");
        
        // First save the post
        PostService.savePost(currentPostData, postResult -> {
            if (!postResult.success) {
                // Show error message
                saveButton.setEnabled(true);
                saveButton.setText("Save Post");
                JOptionPane.showMessageDialog(this, 
                    postResult.message, "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // IMPORTANT FIX: Get the post ID and make sure it's valid
            int postId = postResult.postId;
            System.out.println("DEBUG - Post saved successfully - Post ID from result: " + postId);
            
            // Update current post data with the new post ID
            currentPostData.postId = postId;
            
            System.out.println("DEBUG - After updating currentPostData - Post ID: " + currentPostData.postId);
            System.out.println("DEBUG - After saving post - Image URLs: " + currentPostData.imagePaths);
            
            // If there are no images, we're done
            if (currentPostData.imagePaths.isEmpty()) {
                System.out.println("DEBUG - No images to process");
                saveButton.setEnabled(true);
                saveButton.setText("Save Post");
                
                // Show success message
                JOptionPane.showMessageDialog(this, 
                    postResult.message, "Success", JOptionPane.INFORMATION_MESSAGE);
                
                // Refresh sidebar
                if (sidebarModel != null) {
                    PostService.loadPosts(sidebarModel);
                    // Refresh the platform selector to ensure it has the latest data
                    platformSelector.refresh();
                }
                
                return;
            }
            
            currentPostData.postId = postId;
            imageUploader.setCurrentPostId(postId);
            
            List<Integer> selectedPlatforms = platformSelector.getSelectedPlatformIds();
            currentPostData.platformIds = selectedPlatforms;

            PostService.updatePostPlatforms(postId, selectedPlatforms, success -> {
                if (!success) {
                    System.out.println("DEBUG - Failed to update platform selections for post ID: " + postId);
                } else {
                    System.out.println("DEBUG - Platform selections updated successfully for post ID: " + postId);
                }
            });
            
            // IMPORTANT FIX: Verify that we have a valid post ID
            if (postId <= 0) {
                System.out.println("DEBUG - Invalid post ID: " + postId);
                saveButton.setEnabled(true);
                saveButton.setText("Save Post");
                
                JOptionPane.showMessageDialog(this, 
                    "Post saved but couldn't get a valid ID for image linking. Please try again.", 
                    "Warning", JOptionPane.WARNING_MESSAGE);
                    
                // Refresh sidebar
                if (sidebarModel != null) {
                    PostService.loadPosts(sidebarModel);
                }
                
                return;
            }
            
            // Copy the post ID to a final variable for use in lambdas
            final int finalPostId = postId;
            System.out.println("DEBUG - Using finalPostId for image processing: " + finalPostId);
            
            // Counter for tracking completed image operations
            final int[] imagesProcessed = {0};
            final int totalImages = currentPostData.imagePaths.size();
            final boolean[] hasErrors = {false};
            
            System.out.println("DEBUG - Starting to process " + totalImages + " images for post ID: " + finalPostId);
            
            // For each image URL, save the image and link it to the post
            for (String imageUrl : currentPostData.imagePaths) {
                System.out.println("DEBUG - Processing image URL: " + imageUrl + " for post ID: " + finalPostId);
                
                // First save the image
                lib.ImageService.addImage(imageUrl, "", imageResult -> {
                    if (imageResult.success) {
                        // Then link it to the post
                        int imageId = imageResult.imageId;
                        System.out.println("DEBUG - Image added successfully - Image ID: " + imageId + " for post ID: " + finalPostId);
                        
                        // IMPORTANT: Use finalPostId to ensure correct post ID is used
                        lib.ImageService.linkImageToPost(finalPostId, imageId, linkResult -> {
                            synchronized (imagesProcessed) {
                                imagesProcessed[0]++;
                                
                                if (linkResult.success) {
                                    System.out.println("DEBUG - Image linked successfully - Image ID: " + imageId + ", Post ID: " + finalPostId);
                                } else {
                                    System.out.println("DEBUG - Failed to link image - Image ID: " + imageId + ", Post ID: " + finalPostId);
                                    System.out.println("DEBUG - Error: " + linkResult.message);
                                    hasErrors[0] = true;
                                }
                                
                                // If all images have been processed, update UI
                                if (imagesProcessed[0] >= totalImages) {
                                    System.out.println("DEBUG - All images processed - Total: " + totalImages + ", Errors: " + hasErrors[0]);
                                    
                                    SwingUtilities.invokeLater(() -> {
                                        saveButton.setEnabled(true);
                                        saveButton.setText("Save Post");
                                        
                                        // Show appropriate message
                                        if (hasErrors[0]) {
                                            JOptionPane.showMessageDialog(FormPanel.this, 
                                                "Post saved but some images could not be linked.", 
                                                "Partial Success", JOptionPane.WARNING_MESSAGE);
                                        } else {
                                            JOptionPane.showMessageDialog(FormPanel.this, 
                                                postResult.message, "Success", JOptionPane.INFORMATION_MESSAGE);
                                        }
                                        
                                        // Refresh sidebar
                                        if (sidebarModel != null) {
                                            PostService.loadPosts(sidebarModel);
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        System.out.println("DEBUG - Failed to add image: " + imageUrl);
                        System.out.println("DEBUG - Error: " + imageResult.message);
                        
                        synchronized (imagesProcessed) {
                            imagesProcessed[0]++;
                            hasErrors[0] = true;
                            
                            // If all images have been processed, update UI
                            if (imagesProcessed[0] >= totalImages) {
                                System.out.println("DEBUG - All images processed - Total: " + totalImages + ", Errors: " + hasErrors[0]);
                                
                                SwingUtilities.invokeLater(() -> {
                                    saveButton.setEnabled(true);
                                    saveButton.setText("Save Post");
                                    
                                    // Show appropriate message
                                    JOptionPane.showMessageDialog(FormPanel.this, 
                                        "Post saved but some images could not be saved.", 
                                        "Partial Success", JOptionPane.WARNING_MESSAGE);
                                    
                                    // Refresh sidebar
                                    if (sidebarModel != null) {
                                        PostService.loadPosts(sidebarModel);
                                    }
                                });
                            }
                        }
                    }
                });
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
            
            // If the post has an ID, load images from the API
            if (postData.postId > 0) {
                // Set the post ID in the image uploader
                imageUploader.setCurrentPostId(postData.postId);
                
                // Load images from API
                lib.ImageService.getImagesForPost(postData.postId, imageResult -> {
                    if (imageResult.success && imageResult.images != null) {
                        // Use the new method to load images with their IDs
                        imageUploader.loadImagesFromData(imageResult.images);
                    } else {
                        // Clear any existing images
                        imageUploader.clearImages();
                    }
                });
                
                platformSelector.clearSelections();
                
                PostService.getPostPlatforms(postData.postId, platformIds -> {
                    // Set selected platforms
                    platformSelector.setSelectedPlatforms(platformIds);
                    
                    // Store in current post data
                    currentPostData.platformIds = platformIds;
                });
            } else {
                // For compatibility with older data format
                imageUploader.loadImagesFromUrls(postData.imagePaths);
                platformSelector.setSelectedPlatforms(postData.platformIds);
            }
            
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
            // Optionally refresh the platform list
            platformSelector.refresh();
            
            imageUploader.clearImages();
            imageUploader.setCurrentPostId(-1);
            
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