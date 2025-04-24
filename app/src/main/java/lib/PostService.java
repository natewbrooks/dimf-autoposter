package lib;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Service class to handle all API interactions for posts
 */
public class PostService {
    private static final String API_BASE_URL = "http://localhost:8000/api";
    
    /**
     * Load all posts into the provided model
     * @param model The list model to populate with post names
     */
    public static void loadPosts(DefaultListModel<String> model) {
        try {
            URL url = new URL(API_BASE_URL + "/posts");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
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
                            
                            // Load platforms
                            postData.platformIds = loadPlatformsForPost(postData.postId);
                            
                            // Load images
                            postData.imagePaths = loadImagesForPost(postData.postId);
                            
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
     * Load platforms for a post
     * @param postId Post ID
     * @return List of platform IDs
     */
    private static List<Integer> loadPlatformsForPost(int postId) {
        List<Integer> platformIds = new ArrayList<>();
        try {
            URL platformsUrl = new URL(API_BASE_URL + "/posts/" + postId + "/platforms/");
            HttpURLConnection platformsConn = (HttpURLConnection) platformsUrl.openConnection();
            platformsConn.setRequestMethod("GET");
            
            BufferedReader platformsReader = new BufferedReader(new InputStreamReader(platformsConn.getInputStream()));
            StringBuilder platformsResponse = new StringBuilder();
            String platformsLine;
            while ((platformsLine = platformsReader.readLine()) != null) {
                platformsResponse.append(platformsLine);
            }
            platformsReader.close();
            
            JSONArray platformsArray = new JSONArray(platformsResponse.toString());
            for (int j = 0; j < platformsArray.length(); j++) {
                platformIds.add(platformsArray.getJSONObject(j).getInt("PlatformID"));
            }
        } catch (Exception e) {
            System.err.println("Error loading platforms for post: " + e.getMessage());
        }
        return platformIds;
    }
    
    /**
     * Load images for a post
     * @param postId Post ID
     * @return List of image paths
     */
    private static List<String> loadImagesForPost(int postId) {
        List<String> imagePaths = new ArrayList<>();
        try {
            URL imagesUrl = new URL(API_BASE_URL + "/posts/" + postId + "/images/");
            HttpURLConnection imagesConn = (HttpURLConnection) imagesUrl.openConnection();
            imagesConn.setRequestMethod("GET");
            
            BufferedReader imagesReader = new BufferedReader(new InputStreamReader(imagesConn.getInputStream()));
            StringBuilder imagesResponse = new StringBuilder();
            String imagesLine;
            while ((imagesLine = imagesReader.readLine()) != null) {
                imagesResponse.append(imagesLine);
            }
            imagesReader.close();
            
            JSONArray imagesArray = new JSONArray(imagesResponse.toString());
            for (int j = 0; j < imagesArray.length(); j++) {
                imagePaths.add(imagesArray.getJSONObject(j).getString("URL"));
            }
        } catch (Exception e) {
            System.err.println("Error loading images for post: " + e.getMessage());
        }
        return imagePaths;
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

                    // Create JSON object for request
                    JSONObject requestObj = new JSONObject();
                    requestObj.put("name", postData.name);
                    requestObj.put("date_of_death", postData.dateOfDeath);
                    requestObj.put("content", postData.content);
                    
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
                    
                    // Convert to JSON string
                    String requestBody = requestObj.toString();

                    try (OutputStream os = conn.getOutputStream()) {
                        byte[] input = requestBody.getBytes("utf-8");
                        os.write(input, 0, input.length);
                    }

                    int responseCode = conn.getResponseCode();
                    
                    if (responseCode >= 200 && responseCode < 300) {
                        // Success
                        BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                        StringBuilder responseBuilder = new StringBuilder();
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            responseBuilder.append(inputLine);
                        }
                        in.close();
                        
                        JSONObject responseJson = new JSONObject(responseBuilder.toString());
                        int newPostId = responseJson.optInt("PostID", -1);
                        
                        SaveResult result = new SaveResult();
                        result.success = true;
                        result.message = isUpdate ? "Post updated successfully" : "Post created successfully";
                        result.postId = newPostId > 0 ? newPostId : postData.postId;
                        
                        return result;
                    } else {
                        // Error
                        BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream()));
                        StringBuilder errorResponse = new StringBuilder();
                        String errorLine;
                        while ((errorLine = errorReader.readLine()) != null) {
                            errorResponse.append(errorLine);
                        }
                        errorReader.close();
                        
                        SaveResult result = new SaveResult();
                        result.success = false;
                        result.message = "Error: " + responseCode + " - " + errorResponse.toString();
                        
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
    
    /**
     * Data class to hold post information
     */
    public static class PostData {
        public int postId = -1;
        public String name = "";
        public String dateOfDeath = "";
        public String content = "";
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
}