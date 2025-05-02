package lib;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONObject;

import lib.UserService;

/**
 * Service class to handle all post-related operations
 */
public class PostService {
    private static final String API_BASE_URL = "http://localhost:8000/api";
    
    /**
     * Data class to hold post information
     */
    public static class PostData {
        public int postId = -1;
        public String name = "";
        public String dateOfDeath = "";
        public String content = "";
        public String creatorUsername;
        public int creatorUserId = -1;
        public List<Integer> platformIds = new ArrayList<>();
        public List<String> imagePaths = new ArrayList<>();
        
        // Search/AI cache information
        public String lastQuery = null;
        public String lastSummary = null;
    }
    
    /**
     * Result class for save operations
     */
    public static class SaveResult {
        public boolean success;
        public String message;
        public int postId = -1;
    }
    
    /**
     * Get the API base URL
     * @return The API base URL
     */
    public static String getApiBaseUrl() {
        return API_BASE_URL;
    }
    
    /**
     * Load all posts into the provided model
     * @param model The list model to populate with post names
     */
    public static void loadPosts(DefaultListModel<String> model) {
        try {
            URL url = new URL(API_BASE_URL + "/posts");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            // Add auth token from UserService
            String token = UserService.getCurrentToken();
            if (token != null && !token.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
            in.close();
            
            JSONArray posts = new JSONArray(response.toString());
            model.clear();
            for (int i = 0; i < posts.length(); i++) {
                JSONObject post = posts.getJSONObject(i);
                model.addElement(post.getString("Name")); // or "PostID"
            }
        } catch (Exception e) {
            model.addElement("Failed to load posts");
            e.printStackTrace();
        }
    }
    
    /**
     * Load a post by its name
     * @param name Name of the post to load
     * @param callback Callback to receive the post data
     */
    public static void loadPostByName(String name, Consumer<PostData> callback) {
        SwingWorker<PostData, Void> loadWorker = new SwingWorker<>() {
            @Override
            protected PostData doInBackground() {
                try {
                    // First get all posts to find the one with matching name
                    URL getUrl = new URL(API_BASE_URL + "/posts/");
                    HttpURLConnection getConn = (HttpURLConnection) getUrl.openConnection();
                    getConn.setRequestMethod("GET");
                    getConn.setRequestProperty("Accept", "application/json");
                    
                    // Add auth token from UserService
                    String token = UserService.getCurrentToken();
                    if (token != null && !token.isEmpty()) {
                        getConn.setRequestProperty("Authorization", "Bearer " + token);
                    }
                    
                    BufferedReader in = new BufferedReader(new InputStreamReader(getConn.getInputStream()));
                    StringBuilder getResponse = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        getResponse.append(line);
                    }
                    in.close();
                    
                    JSONArray posts = new JSONArray(getResponse.toString());
                    
                    // Find the post with matching name
                    for (int i = 0; i < posts.length(); i++) {
                        JSONObject post = posts.getJSONObject(i);
                        if (post.getString("Name").equals(name)) {
                            // Found the post, create PostData object
                            PostData postData = new PostData();
                            postData.postId = post.getInt("PostID");
                            postData.name = post.getString("Name");
                            postData.dateOfDeath = post.getString("DateOfDeath");
                            postData.content = post.has("Content") ? post.getString("Content") : "";
                            
                            // Get creator information
                            if (post.has("CreatedBy") && !post.isNull("CreatedBy")) {
                                postData.creatorUserId = post.getInt("CreatedBy");
                            }
                            
                            if (post.has("CreatorUsername") && !post.isNull("CreatorUsername")) {
                                postData.creatorUsername = post.getString("CreatorUsername");
                            }
                            
                            // Load images
                            loadPostImages(postData);
                            
                            // Load platforms
                            loadPostPlatforms(postData);
                            
                            return postData;
                        }
                    }
                    
                    // Post not found
                    return null;
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
            
            @Override
            protected void done() {
                try {
                    PostData result = get();
                    callback.accept(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.accept(null);
                }
            }
        };
        
        loadWorker.execute();
    }
    
    /**
     * Load images for a post
     * @param postData Post data object to populate with images
     */
    private static void loadPostImages(PostData postData) {
        try {
            URL url = new URL(API_BASE_URL + "/posts/" + postData.postId + "/images/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            // Add auth token
            String token = UserService.getCurrentToken();
            if (token != null && !token.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            
            JSONArray images = new JSONArray(response.toString());
            postData.imagePaths.clear();
            
            for (int i = 0; i < images.length(); i++) {
                JSONObject image = images.getJSONObject(i);
                postData.imagePaths.add(image.getString("URL"));
            }
            
        } catch (Exception e) {
            System.out.println("Error loading images: " + e.getMessage());
        }
    }
    
    /**
     * Load platforms for a post
     * @param postData Post data object to populate with platforms
     */
    private static void loadPostPlatforms(PostData postData) {
        try {
            URL url = new URL(API_BASE_URL + "/posts/" + postData.postId + "/platforms/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            // Add auth token
            String token = UserService.getCurrentToken();
            if (token != null && !token.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();
            
            JSONArray platforms = new JSONArray(response.toString());
            postData.platformIds.clear();
            
            for (int i = 0; i < platforms.length(); i++) {
                JSONObject platform = platforms.getJSONObject(i);
                postData.platformIds.add(platform.getInt("PlatformID"));
            }
            
        } catch (Exception e) {
            System.out.println("Error loading platforms: " + e.getMessage());
        }
    }
    
    /**
     * Get platforms for a post - delegates to PlatformService
     * @param postId Post ID
     * @param callback Callback with platform IDs
     */
    public static void getPostPlatforms(int postId, Consumer<List<Integer>> callback) {
        // Delegate to PlatformService
        PlatformService.getPlatformsForPost(postId, callback);
    }
    
    /**
     * Update platforms for a post - delegates to PlatformService
     * @param postId Post ID
     * @param platformIds List of platform IDs
     * @param callback Callback with success/failure
     */
    public static void updatePostPlatforms(int postId, List<Integer> platformIds, Consumer<Boolean> callback) {
        // Delegate to PlatformService
        PlatformService.updatePostPlatforms(postId, platformIds, callback);
    }
    
    /**
     * Generate post content using search and AI
     * @param name Name
     * @param dod Date of death string
     * @param callback Callback to receive the generated content
     */
    public static void generatePostContent(String name, String dod, Consumer<String> callback) {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                try {
                    String query = java.net.URLEncoder.encode(name + " " + dod, "UTF-8");
                    URL getUrl = new URL(API_BASE_URL + "/google/search?q=" + query);
                    HttpURLConnection getConn = (HttpURLConnection) getUrl.openConnection();
                    getConn.setRequestMethod("GET");
                    getConn.setRequestProperty("Accept", "application/json");
                    
                    // Add auth token from UserService
                    String token = UserService.getCurrentToken();
                    if (token != null && !token.isEmpty()) {
                        getConn.setRequestProperty("Authorization", "Bearer " + token);
                    }

                    int responseCode = getConn.getResponseCode();
                    
                    if (responseCode != 200) {
                        BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(getConn.getErrorStream()));
                        StringBuilder errorResponse = new StringBuilder();
                        String errorLine;
                        while ((errorLine = errorReader.readLine()) != null) {
                            errorResponse.append(errorLine);
                        }
                        errorReader.close();
                        throw new Exception("HTTP Error: " + responseCode + " - " + errorResponse.toString());
                    }

                    BufferedReader in = new BufferedReader(new InputStreamReader(getConn.getInputStream()));
                    StringBuilder getResponse = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        getResponse.append(line);
                    }
                    in.close();

                    String getJson = getResponse.toString();
                    // Check if the response contains the expected data
                    if (getJson.contains("\"q\":\"") && getJson.contains("\"summary\":\"")) {
                        String lastQuery = getJson.split("\\\"q\\\":\\\"")[1].split("\\\"", 2)[0];
                        String lastSummary = getJson.split("\"summary\":\"")[1].replaceAll("\\\\n", "\n").replaceAll("\"}$", "");
                        
                        return generateFromLLM(lastQuery, lastSummary);
                    } else {
                        throw new Exception("Invalid response format from search API: " + getJson);
                    }

                } catch (Exception e) {
                    return "Error: " + e.getMessage();
                }
            }
            
            @Override
            protected void done() {
                try {
                    String result = get();
                    callback.accept(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.accept("Error: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }
    
    /**
     * Generate post content from LLM
     * @param query Search query
     * @param summary Search summary
     * @return Generated content
     */
    private static String generateFromLLM(String query, String summary) throws Exception {
        URL postUrl = new URL(API_BASE_URL + "/ai/");
        HttpURLConnection postConn = (HttpURLConnection) postUrl.openConnection();
        postConn.setRequestMethod("POST");
        postConn.setRequestProperty("Content-Type", "application/json");
        postConn.setDoOutput(true);
        
        // Add auth token from UserService
        String token = UserService.getCurrentToken();
        if (token != null && !token.isEmpty()) {
            postConn.setRequestProperty("Authorization", "Bearer " + token);
        }

        // Properly escape JSON values
        String escapedQuery = escapeJson(query);
        String escapedSummary = escapeJson(summary);
        
        String postBody = String.format("{\"q\":\"%s\", \"summary\":\"%s\"}", escapedQuery, escapedSummary);
        
        try (OutputStream os = postConn.getOutputStream()) {
            byte[] input = postBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int responseCode = postConn.getResponseCode();
        
        if (responseCode != 200) {
            BufferedReader errorReader = new BufferedReader(
                new InputStreamReader(postConn.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorResponse.append(errorLine);
            }
            errorReader.close();
            throw new Exception("AI API Error: " + responseCode + " - " + errorResponse.toString());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(postConn.getInputStream(), "utf-8"));
        StringBuilder postResponse = new StringBuilder();
        String postLine;
        while ((postLine = reader.readLine()) != null) {
            postResponse.append(postLine);
        }

        String response = postResponse.toString();
        if (response.contains("\"response\":\"")) {
            return response
                    .replace("{\"response\":\"", "")
                    .replaceAll("\"}$", "")
                    .replace("\\n", "\n")
                    .replace("\\\"", "\"");
        } else {
            throw new Exception("Invalid response format from AI API: " + response);
        }
    }
    
    /**
     * Save or update a post
     * @param postData Post data to save
     * @param callback Callback with success/failure message
     */
    public static void savePost(PostData postData, Consumer<SaveResult> callback) {
        System.out.println("DEBUG - PostService.savePost - Post ID: " + postData.postId);
        System.out.println("DEBUG - PostService.savePost - Image Paths: " + postData.imagePaths);
        
        // Get current user information
        int currentUserId = UserService.getCurrentUserId();
        String currentUsername = UserService.getCurrentUsername();
        
        // Update creator information if needed
        if (postData.creatorUserId <= 0 && currentUserId > 0) {
            postData.creatorUserId = currentUserId;
        }
        
        if ((postData.creatorUsername == null || postData.creatorUsername.isEmpty()) && 
                currentUsername != null && !currentUsername.isEmpty()) {
            postData.creatorUsername = currentUsername;
        }
        
        SwingWorker<SaveResult, Void> saveWorker = new SwingWorker<>() {
            @Override
            protected SaveResult doInBackground() {
                try {
                    URL saveUrl;
                    HttpURLConnection conn;
                    
                    boolean isUpdate = postData.postId > 0;
                    
                    if (isUpdate) {
                        // Update existing post
                        saveUrl = new URL(API_BASE_URL + "/posts/" + postData.postId + "/");
                        conn = (HttpURLConnection) saveUrl.openConnection();
                        conn.setRequestMethod("PUT");
                    } else {
                        // Create new post
                        saveUrl = new URL(API_BASE_URL + "/posts/");
                        conn = (HttpURLConnection) saveUrl.openConnection();
                        conn.setRequestMethod("POST");
                    }
                    
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    
                    // Add auth token
                    String token = UserService.getCurrentToken();
                    if (token != null && !token.isEmpty()) {
                        conn.setRequestProperty("Authorization", "Bearer " + token);
                    }

                    // Create JSON object for request
                    JSONObject requestObj = new JSONObject();
                    requestObj.put("name", postData.name);
                    requestObj.put("date_of_death", postData.dateOfDeath);
                    requestObj.put("content", postData.content);
                    
                    // Add creator information
                    if (postData.creatorUserId > 0) {
                        requestObj.put("created_by", postData.creatorUserId);
                    } else if (currentUserId > 0) {
                        requestObj.put("created_by", currentUserId);
                    }
                    
                    if (postData.creatorUsername != null && !postData.creatorUsername.isEmpty()) {
                        requestObj.put("creator_username", postData.creatorUsername);
                    } else if (currentUsername != null && !currentUsername.isEmpty()) {
                        requestObj.put("creator_username", currentUsername);
                    }
                    
                    // Add platforms array
                    JSONArray platformsArray = new JSONArray();
                    for (Integer platformId : postData.platformIds) {
                        platformsArray.put(platformId);
                    }
                    requestObj.put("platforms", platformsArray);
                    
                    // Add images array
                    JSONArray imagesArray = new JSONArray();
                    for (String imagePath : postData.imagePaths) {
                        imagesArray.put(imagePath);
                    }
                    requestObj.put("images", imagesArray);
                    
                    String requestBody = requestObj.toString();
                    System.out.println("DEBUG - PostService.savePost - Full JSON payload: " + requestBody);

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = requestBody.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int responseCode = conn.getResponseCode();
                    System.out.println("DEBUG - PostService.savePost - Response code: " + responseCode);
                    
                    // Read response body regardless of success/failure
                    StringBuilder responseBuilder = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(
                                    responseCode >= 200 && responseCode < 300 
                                    ? conn.getInputStream() 
                                    : conn.getErrorStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            responseBuilder.append(line);
                        }
                    }
                    
                    String response = responseBuilder.toString();
                    System.out.println("DEBUG - PostService.savePost - Response: " + response);
                    
                    if (responseCode >= 200 && responseCode < 300) {
                        // Successful response
                        int newPostId = -1;
                        try {
                            JSONObject responseJson = new JSONObject(response);
                            
                            // Try different possible field names for post ID
                            if (responseJson.has("post_id")) {
                                newPostId = responseJson.getInt("post_id");
                            } else if (responseJson.has("PostID")) {
                                newPostId = responseJson.getInt("PostID");
                            } else if (isUpdate) {
                                // For updates, use existing ID
                                newPostId = postData.postId;
                            }
                        } catch (Exception e) {
                            System.out.println("DEBUG - Error parsing response JSON: " + e.getMessage());
                        }
                        
                        // If we still don't have a post ID, try to get it by querying for the post
                        if (newPostId <= 0) {
                            newPostId = retrievePostIdByName(postData.name);
                            System.out.println("DEBUG - Retrieved post ID by name: " + newPostId);
                        }
                        
                        SaveResult result = new SaveResult();
                        result.success = true;
                        result.message = isUpdate ? "Post updated successfully" : "Post created successfully";
                        result.postId = newPostId;
                        
                        return result;
                    } else {
                        // If response indicates post created but ID retrieval failed, try to retrieve ID separately
                        if (responseCode == 500 && response.contains("Failed to retrieve inserted post ID")) {
                            int retrievedPostId = retrievePostIdByName(postData.name);
                            if (retrievedPostId > 0) {
                                SaveResult result = new SaveResult();
                                result.success = true;
                                result.message = "Post created, but ID had to be retrieved separately";
                                result.postId = retrievedPostId;
                                return result;
                            }
                        }
                        
                        // General error response
                        SaveResult result = new SaveResult();
                        result.success = false;
                        result.message = "Error: " + responseCode + " - " + response;
                        return result;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    
                    SaveResult result = new SaveResult();
                    result.success = false;
                    result.message = "Error: " + e.getMessage();
                    return result;
                }
            }
            
            @Override
            protected void done() {
                try {
                    SaveResult result = get();
                    callback.accept(result);
                } catch (Exception e) {
                    e.printStackTrace();
                    SaveResult result = new SaveResult();
                    result.success = false;
                    result.message = "Error: " + e.getMessage();
                    callback.accept(result);
                }
            }
        };

        saveWorker.execute();
    }

    // Helper method to retrieve post ID by name when server fails to return it
    private static int retrievePostIdByName(String name) {
        System.out.println("DEBUG - Attempting to retrieve post ID by name: " + name);
        try {
            // Create URL for getting all posts
            URL url = new URL(API_BASE_URL + "/posts/");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            
            // Add auth token
            String token = UserService.getCurrentToken();
            if (token != null && !token.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + token);
            }
            
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                
                JSONArray posts = new JSONArray(response.toString());
                
                // Look for the post with matching name, get the most recent
                int mostRecentPostId = -1;
                for (int i = 0; i < posts.length(); i++) {
                    JSONObject post = posts.getJSONObject(i);
                    if (post.getString("Name").equals(name)) {
                        int currentPostId = post.getInt("PostID");
                        if (currentPostId > mostRecentPostId) {
                            mostRecentPostId = currentPostId;
                        }
                    }
                }
                
                System.out.println("DEBUG - Retrieved post ID: " + mostRecentPostId + " for name: " + name);
                return mostRecentPostId;
            }
        } catch (Exception e) {
            System.out.println("DEBUG - Error retrieving post ID by name: " + e.getMessage());
        }
        return -1;
    }
    
    /**
     * Properly escapes a string for JSON inclusion
     */
    private static String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        
        return input
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\b", "\\b")
            .replace("\f", "\\f");
    }
}