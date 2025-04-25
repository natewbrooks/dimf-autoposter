package lib;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Service responsible for all platform-related operations
 */
public class PlatformService {
    private static final String API_BASE_URL = "http://localhost:8000/api";
    
    /**
     * Data class for platform information
     */
    public static class PlatformData {
        public int platformId;
        public String name;
        public boolean apiAccessStatus;
        public String platformUrl;
        public String iconUrl;
    }
    
    /**
     * Result class for platform operations
     */
    public static class PlatformResult {
        public boolean success;
        public String message;
        public List<PlatformData> platforms;
        
        public PlatformResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
    
    /**
     * Get all available platforms
     * @param callback Callback with platform data
     */
    public static void getAllPlatforms(Consumer<PlatformResult> callback) {
        System.out.println("DEBUG - PlatformService.getAllPlatforms");
        
        new Thread(() -> {
            try {
                // Create connection
                URL apiUrl = new URL(API_BASE_URL + "/platforms/");
                HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
                conn.setRequestMethod("GET");
                
                // Get response
                int responseCode = conn.getResponseCode();
                System.out.println("DEBUG - PlatformService.getAllPlatforms - Response code: " + responseCode);
                
                // Read response body
                StringBuilder responseBody = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                responseCode >= 200 && responseCode < 300 
                                ? conn.getInputStream() 
                                : conn.getErrorStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        responseBody.append(line);
                    }
                }
                
                String responseBodyStr = responseBody.toString();
                System.out.println("DEBUG - PlatformService.getAllPlatforms - Response body: " + responseBodyStr);
                
                // Process response
                if (responseCode >= 200 && responseCode < 300) {
                    try {
                        PlatformResult result = new PlatformResult(true, "Platforms loaded");
                        JSONArray platformsArray = new JSONArray(responseBodyStr);
                        
                        // Parse the platform data
                        List<PlatformData> platforms = new ArrayList<>();
                        for (int i = 0; i < platformsArray.length(); i++) {
                            JSONObject platformObj = platformsArray.getJSONObject(i);
                            PlatformData platform = new PlatformData();
                            platform.platformId = platformObj.getInt("PlatformID");
                            platform.name = platformObj.getString("Name");
                            // Convert integer to boolean (0 = false, any other value = true)
                            platform.apiAccessStatus = platformObj.getInt("APIAccessStatus") != 0;
                            platform.platformUrl = platformObj.has("PlatformURL") ? platformObj.getString("PlatformURL") : "";
                            platform.iconUrl = platformObj.has("IconURL") ? platformObj.getString("IconURL") : "";
                            platforms.add(platform);
                        }
                        
                        result.platforms = platforms;
                        SwingUtilities.invokeLater(() -> callback.accept(result));
                    } catch (Exception e) {
                        System.out.println("DEBUG - PlatformService.getAllPlatforms - Exception parsing response: " + e.getMessage());
                        e.printStackTrace();
                        SwingUtilities.invokeLater(() -> 
                            callback.accept(new PlatformResult(false, "Failed to load platforms: " + e.getMessage())));
                    }
                } else {
                    String errorMsg = "Failed to get platforms. Status code: " + responseCode;
                    if (!responseBodyStr.isEmpty()) {
                        errorMsg += " - " + responseBodyStr;
                    }
                    final String finalErrorMsg = errorMsg;
                    SwingUtilities.invokeLater(() -> callback.accept(new PlatformResult(false, finalErrorMsg)));
                }
            } catch (Exception e) {
                System.out.println("DEBUG - PlatformService.getAllPlatforms - Exception: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> 
                    callback.accept(new PlatformResult(false, "Error: " + e.getMessage())));
            }
        }).start();
    }
    
    /**
     * Get platforms associated with a specific post
     * @param postId Post ID
     * @param callback Callback with platform IDs
     */
    public static void getPlatformsForPost(int postId, Consumer<List<Integer>> callback) {
        System.out.println("DEBUG - PlatformService.getPlatformsForPost - Post ID: " + postId);
        
        new Thread(() -> {
            try {
                // Create connection
                URL apiUrl = new URL(API_BASE_URL + "/posts/" + postId + "/platforms/");
                HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
                conn.setRequestMethod("GET");
                
                // Get response
                int responseCode = conn.getResponseCode();
                System.out.println("DEBUG - PlatformService.getPlatformsForPost - Response code: " + responseCode);
                
                // Read response body
                StringBuilder responseBody = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                responseCode >= 200 && responseCode < 300 
                                ? conn.getInputStream() 
                                : conn.getErrorStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        responseBody.append(line);
                    }
                }
                
                String responseBodyStr = responseBody.toString();
                System.out.println("DEBUG - PlatformService.getPlatformsForPost - Response body: " + responseBodyStr);
                
                // Process response
                if (responseCode >= 200 && responseCode < 300) {
                    try {
                        List<Integer> platformIds = new ArrayList<>();
                        JSONArray platformsArray = new JSONArray(responseBodyStr);
                        
                        // Extract platform IDs
                        for (int i = 0; i < platformsArray.length(); i++) {
                            JSONObject platformObj = platformsArray.getJSONObject(i);
                            platformIds.add(platformObj.getInt("PlatformID"));
                        }
                        
                        SwingUtilities.invokeLater(() -> callback.accept(platformIds));
                    } catch (Exception e) {
                        System.out.println("DEBUG - PlatformService.getPlatformsForPost - Exception parsing response: " + e.getMessage());
                        e.printStackTrace();
                        SwingUtilities.invokeLater(() -> callback.accept(new ArrayList<>()));
                    }
                } else {
                    System.out.println("DEBUG - PlatformService.getPlatformsForPost - Error response: " + responseBodyStr);
                    SwingUtilities.invokeLater(() -> callback.accept(new ArrayList<>()));
                }
            } catch (Exception e) {
                System.out.println("DEBUG - PlatformService.getPlatformsForPost - Exception: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> callback.accept(new ArrayList<>()));
            }
        }).start();
    }
    
    /**
     * Update platforms for a post
     * @param postId Post ID
     * @param platformIds List of platform IDs
     * @param callback Callback with success/failure
     */
    public static void updatePostPlatforms(int postId, List<Integer> platformIds, Consumer<Boolean> callback) {
        System.out.println("DEBUG - PlatformService.updatePostPlatforms - Post ID: " + postId + ", Platforms: " + platformIds);
        
        new Thread(() -> {
            try {
                // Create JSON payload
                JSONObject payload = new JSONObject();
                JSONArray platformArray = new JSONArray();
                for (Integer id : platformIds) {
                    platformArray.put(id);
                }
                payload.put("platform_ids", platformArray);
                String jsonBody = payload.toString();
                
                System.out.println("DEBUG - PlatformService.updatePostPlatforms - Request body: " + jsonBody);
                
                // Create connection
                URL apiUrl = new URL(API_BASE_URL + "/posts/" + postId + "/platforms/");
                HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                
                // Send request body
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonBody.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }
                
                // Get response
                int responseCode = conn.getResponseCode();
                System.out.println("DEBUG - PlatformService.updatePostPlatforms - Response code: " + responseCode);
                
                // Read response body
                StringBuilder responseBody = new StringBuilder();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                responseCode >= 200 && responseCode < 300 
                                ? conn.getInputStream() 
                                : conn.getErrorStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        responseBody.append(line);
                    }
                }
                
                String responseBodyStr = responseBody.toString();
                System.out.println("DEBUG - PlatformService.updatePostPlatforms - Response body: " + responseBodyStr);
                
                // Process response
                boolean success = responseCode >= 200 && responseCode < 300;
                SwingUtilities.invokeLater(() -> callback.accept(success));
                
            } catch (Exception e) {
                System.out.println("DEBUG - PlatformService.updatePostPlatforms - Exception: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> callback.accept(false));
            }
        }).start();
    }
    
    /**
     * Get platform data for specific platform IDs
     * 
     * @param platformIds List of platform IDs
     * @param callback Callback with list of platform data
     */
    public static void getPlatformDataByIds(List<Integer> platformIds, Consumer<List<PlatformData>> callback) {
        if (platformIds == null || platformIds.isEmpty()) {
            SwingUtilities.invokeLater(() -> callback.accept(new ArrayList<>()));
            return;
        }
        
        System.out.println("DEBUG - PlatformService.getPlatformDataByIds - Platform IDs: " + platformIds);
        
        // Get all platforms and filter by IDs
        getAllPlatforms(result -> {
            if (!result.success || result.platforms == null) {
                SwingUtilities.invokeLater(() -> callback.accept(new ArrayList<>()));
                return;
            }
            
            // Filter platforms by ID
            List<PlatformData> filteredPlatforms = new ArrayList<>();
            for (PlatformData platform : result.platforms) {
                if (platformIds.contains(platform.platformId)) {
                    filteredPlatforms.add(platform);
                }
            }
            
            SwingUtilities.invokeLater(() -> callback.accept(filteredPlatforms));
        });
    }
}