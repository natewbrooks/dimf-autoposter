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
	
	public class ImageService {
	    private static final String API_BASE_URL = "http://localhost:8000/api";
	    
	    public static class ImageResult {
	        public boolean success;
	        public String message;
	        public int imageId;
	        public List<ImageData> images;
	        
	        public ImageResult(boolean success, String message) {
	            this.success = success;
	            this.message = message;
	        }
	    }
	    
	    public static class ImageData {
	        public int imageId;
	        public String url;
	        public String source;
	    }
	    
	    // Add a new image with callback
	    public static void addImage(String url, String source, Consumer<ImageResult> callback) {
	        System.out.println("DEBUG - ImageService.addImage - URL: " + url);
	        
	        new Thread(() -> {
	            try {
	                // Create JSON payload
	                JSONObject payload = new JSONObject();
	                payload.put("url", url);
	                payload.put("source", source);
	                String jsonBody = payload.toString();
	                
	                System.out.println("DEBUG - ImageService.addImage - Request body: " + jsonBody);
	                
	                // Create connection
	                URL apiUrl = new URL(API_BASE_URL + "/images/");
	                HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
	                conn.setRequestMethod("POST");
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
	                System.out.println("DEBUG - ImageService.addImage - Response code: " + responseCode);
	                
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
	                System.out.println("DEBUG - ImageService.addImage - Response body: " + responseBodyStr);
	                
	                // Process response
	                if (responseCode >= 200 && responseCode < 300) {
	                    try {
	                        JSONObject result = new JSONObject(responseBodyStr);
	                        ImageResult imageResult = new ImageResult(true, result.getString("status"));
	                        imageResult.imageId = result.getInt("image_id");
	                        System.out.println("DEBUG - ImageService.addImage - Image ID: " + imageResult.imageId);
	                        
	                        // Call callback on the main thread
	                        SwingUtilities.invokeLater(() -> callback.accept(imageResult));
	                    } catch (Exception e) {
	                        System.out.println("DEBUG - ImageService.addImage - Exception parsing response: " + e.getMessage());
	                        SwingUtilities.invokeLater(() -> 
	                            callback.accept(new ImageResult(false, "Failed to parse response: " + e.getMessage())));
	                    }
	                } else {
	                    String errorMsg = "Failed to add image. Status code: " + responseCode;
	                    if (!responseBodyStr.isEmpty()) {
	                        errorMsg += " - " + responseBodyStr;
	                    }
	                    final String finalErrorMsg = errorMsg;
	                    SwingUtilities.invokeLater(() -> callback.accept(new ImageResult(false, finalErrorMsg)));
	                }
	            } catch (Exception e) {
	                System.out.println("DEBUG - ImageService.addImage - Exception: " + e.getMessage());
	                SwingUtilities.invokeLater(() -> 
	                    callback.accept(new ImageResult(false, "Error: " + e.getMessage())));
	            }
	        }).start();
	    }
	    
	    // Link an image to a post with callback
	    public static void linkImageToPost(int postId, int imageId, Consumer<ImageResult> callback) {
	        System.out.println("DEBUG - ImageService.linkImageToPost - Post ID: " + postId + ", Image ID: " + imageId);
	        
	        // Validate inputs
	        if (postId <= 0 || imageId <= 0) {
	            System.out.println("DEBUG - ImageService.linkImageToPost - Invalid parameters: postId=" + postId + ", imageId=" + imageId);
	            SwingUtilities.invokeLater(() -> 
	                callback.accept(new ImageResult(false, "Invalid post ID or image ID")));
	            return;
	        }
	        
	        new Thread(() -> {
	            try {
	                // Create JSON payload
	                JSONObject payload = new JSONObject();
	                payload.put("post_id", postId);
	                payload.put("image_id", imageId);
	                String jsonBody = payload.toString();
	                
	                System.out.println("DEBUG - ImageService.linkImageToPost - Request body: " + jsonBody);
	                
	                // Create connection
	                URL apiUrl = new URL(API_BASE_URL + "/posts/images/");
	                HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
	                conn.setRequestMethod("POST");
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
	                System.out.println("DEBUG - ImageService.linkImageToPost - Response code: " + responseCode);
	                
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
	                System.out.println("DEBUG - ImageService.linkImageToPost - Response body: " + responseBodyStr);
	                
	                // Process response
	                if (responseCode >= 200 && responseCode < 300) {
	                    try {
	                        JSONObject result = new JSONObject(responseBodyStr);
	                        SwingUtilities.invokeLater(() -> 
	                            callback.accept(new ImageResult(true, result.getString("status"))));
	                    } catch (Exception e) {
	                        System.out.println("DEBUG - ImageService.linkImageToPost - Exception parsing response: " + e.getMessage());
	                        SwingUtilities.invokeLater(() -> 
	                            callback.accept(new ImageResult(false, "Failed to parse response: " + e.getMessage())));
	                    }
	                } else {
	                    String errorMsg = "Failed to link image. Status code: " + responseCode;
	                    if (!responseBodyStr.isEmpty()) {
	                        errorMsg += " - " + responseBodyStr;
	                    }
	                    final String finalErrorMsg = errorMsg;
	                    SwingUtilities.invokeLater(() -> callback.accept(new ImageResult(false, finalErrorMsg)));
	                }
	            } catch (Exception e) {
	                System.out.println("DEBUG - ImageService.linkImageToPost - Exception: " + e.getMessage());
	                SwingUtilities.invokeLater(() -> 
	                    callback.accept(new ImageResult(false, "Error: " + e.getMessage())));
	            }
	        }).start();
	    }
	    
	    // Get all images for a post with callback
	    public static void getImagesForPost(int postId, Consumer<ImageResult> callback) {
	        new Thread(() -> {
	            try {
	                // Create connection
	                URL apiUrl = new URL(API_BASE_URL + "/posts/" + postId + "/images/");
	                HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
	                conn.setRequestMethod("GET");
	                
	                // Get response
	                int responseCode = conn.getResponseCode();
	                System.out.println("DEBUG - ImageService.getImagesForPost - Response code: " + responseCode);
	                
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
	                System.out.println("DEBUG - ImageService.getImagesForPost - Response body: " + responseBodyStr);
	                
	                // Process response
	                if (responseCode >= 200 && responseCode < 300) {
	                    try {
	                        ImageResult result = new ImageResult(true, "Images loaded");
	                        JSONArray imagesArray = new JSONArray(responseBodyStr);
	                        
	                        // Parse the image data
	                        List<ImageData> images = new ArrayList<>();
	                        for (int i = 0; i < imagesArray.length(); i++) {
	                            JSONObject imageObj = imagesArray.getJSONObject(i);
	                            ImageData image = new ImageData();
	                            image.imageId = imageObj.getInt("ImageID");
	                            image.url = imageObj.getString("URL");
	                            image.source = imageObj.has("Source") ? imageObj.getString("Source") : "";
	                            images.add(image);
	                        }
	                        
	                        result.images = images;
	                        SwingUtilities.invokeLater(() -> callback.accept(result));
	                    } catch (Exception e) {
	                        System.out.println("DEBUG - ImageService.getImagesForPost - Exception parsing response: " + e.getMessage());
	                        SwingUtilities.invokeLater(() -> 
	                            callback.accept(new ImageResult(false, "Failed to load images: " + e.getMessage())));
	                    }
	                } else {
	                    String errorMsg = "Failed to get images. Status code: " + responseCode;
	                    if (!responseBodyStr.isEmpty()) {
	                        errorMsg += " - " + responseBodyStr;
	                    }
	                    final String finalErrorMsg = errorMsg;
	                    SwingUtilities.invokeLater(() -> callback.accept(new ImageResult(false, finalErrorMsg)));
	                }
	            } catch (Exception e) {
	                System.out.println("DEBUG - ImageService.getImagesForPost - Exception: " + e.getMessage());
	                SwingUtilities.invokeLater(() -> 
	                    callback.accept(new ImageResult(false, "Error: " + e.getMessage())));
	            }
	        }).start();
	    }
	    
	    public static void searchGoogleImages(String query, Consumer<ImageResult> callback) {
	        System.out.println("DEBUG - ImageService.searchGoogleImages - Query: " + query);
	        
	        new Thread(() -> {
	            try {
	                // URL encode the query
	                String encodedQuery = java.net.URLEncoder.encode(query, "UTF-8");
	                
	                // Create connection
	                URL apiUrl = new URL(API_BASE_URL + "/google/images/?q=" + encodedQuery);
	                HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
	                conn.setRequestMethod("GET");
	                conn.setRequestProperty("Accept", "application/json");
	                
	                // Get response
	                int responseCode = conn.getResponseCode();
	                System.out.println("DEBUG - ImageService.searchGoogleImages - Response code: " + responseCode);
	                
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
	                System.out.println("DEBUG - ImageService.searchGoogleImages - Response body: " + responseBodyStr);
	                
	                // Process response
	                if (responseCode >= 200 && responseCode < 300) {
	                    try {
	                        JSONObject result = new JSONObject(responseBodyStr);
	                        JSONArray thumbnails = result.getJSONArray("thumbnails");
	                        
	                        // Create image result with the thumbnails
	                        ImageResult imageResult = new ImageResult(true, "Images found");
	                        List<ImageData> images = new ArrayList<>();
	                        
	                        for (int i = 0; i < thumbnails.length(); i++) {
	                            String thumbnailUrl = thumbnails.getString(i);
	                            ImageData image = new ImageData();
	                            image.url = thumbnailUrl;
	                            image.source = "Google Images";
	                            images.add(image);
	                        }
	                        
	                        imageResult.images = images;
	                        
	                        // Call callback on the main thread
	                        SwingUtilities.invokeLater(() -> callback.accept(imageResult));
	                    } catch (Exception e) {
	                        System.out.println("DEBUG - ImageService.searchGoogleImages - Exception parsing response: " + e.getMessage());
	                        SwingUtilities.invokeLater(() -> 
	                            callback.accept(new ImageResult(false, "Failed to parse response: " + e.getMessage())));
	                    }
	                } else {
	                    String errorMsg = "Failed to search images. Status code: " + responseCode;
	                    if (!responseBodyStr.isEmpty()) {
	                        errorMsg += " - " + responseBodyStr;
	                    }
	                    final String finalErrorMsg = errorMsg;
	                    SwingUtilities.invokeLater(() -> callback.accept(new ImageResult(false, finalErrorMsg)));
	                }
	            } catch (Exception e) {
	                System.out.println("DEBUG - ImageService.searchGoogleImages - Exception: " + e.getMessage());
	                SwingUtilities.invokeLater(() -> 
	                    callback.accept(new ImageResult(false, "Error: " + e.getMessage())));
	            }
	        }).start();
	    }
	    
	    public static void getImageIdByUrl(String url, Consumer<ImageResult> callback) {
	        System.out.println("DEBUG - ImageService.getImageIdByUrl - URL: " + url);
	        
	        new Thread(() -> {
	            try {
	                // URL encode the query
	                String encodedUrl = java.net.URLEncoder.encode(url, "UTF-8");
	                
	                // Create connection
	                URL apiUrl = new URL(API_BASE_URL + "/images/find?url=" + encodedUrl);
	                HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
	                conn.setRequestMethod("GET");
	                conn.setRequestProperty("Accept", "application/json");
	                
	                // Get response
	                int responseCode = conn.getResponseCode();
	                System.out.println("DEBUG - ImageService.getImageIdByUrl - Response code: " + responseCode);
	                
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
	                System.out.println("DEBUG - ImageService.getImageIdByUrl - Response: " + responseBodyStr);
	                
	                // Process response
	                if (responseCode >= 200 && responseCode < 300) {
	                    try {
	                        JSONObject result = new JSONObject(responseBodyStr);
	                        ImageResult imageResult = new ImageResult(true, "Image found");
	                        imageResult.imageId = result.getInt("image_id");
	                        System.out.println("DEBUG - ImageService.getImageIdByUrl - Image ID: " + imageResult.imageId);
	                        
	                        // Return result
	                        SwingUtilities.invokeLater(() -> callback.accept(imageResult));
	                    } catch (Exception e) {
	                        System.out.println("DEBUG - ImageService.getImageIdByUrl - Exception parsing: " + e.getMessage());
	                        SwingUtilities.invokeLater(() -> 
	                            callback.accept(new ImageResult(false, "Failed to parse response: " + e.getMessage())));
	                    }
	                } else {
	                    String errorMsg = "Failed to find image. Status code: " + responseCode;
	                    if (!responseBodyStr.isEmpty()) {
	                        errorMsg += " - " + responseBodyStr;
	                    }
	                    final String finalErrorMsg = errorMsg;
	                    SwingUtilities.invokeLater(() -> callback.accept(new ImageResult(false, finalErrorMsg)));
	                }
	            } catch (Exception e) {
	                System.out.println("DEBUG - ImageService.getImageIdByUrl - Exception: " + e.getMessage());
	                SwingUtilities.invokeLater(() -> 
	                    callback.accept(new ImageResult(false, "Error: " + e.getMessage())));
	            }
	        }).start();
	    }
	    
	    public static void deleteImage(int postId, int imageId, Consumer<ImageResult> callback) {
	        System.out.println("DEBUG - ImageService.deleteImage - Post ID: " + postId + ", Image ID: " + imageId);
	        
	        new Thread(() -> {
	            try {
	                // Create JSON payload
	                JSONObject payload = new JSONObject();
	                payload.put("post_id", postId);
	                payload.put("image_id", imageId);
	                String jsonBody = payload.toString();
	                
	                System.out.println("DEBUG - ImageService.deleteImage - Request body: " + jsonBody);
	                
	                // Create connection
	                URL apiUrl = new URL(API_BASE_URL + "/posts/images/");
	                HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
	                conn.setRequestMethod("DELETE");
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
	                System.out.println("DEBUG - ImageService.deleteImage - Response code: " + responseCode);
	                
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
	                System.out.println("DEBUG - ImageService.deleteImage - Response body: " + responseBodyStr);
	                
	                // Process response
	                if (responseCode >= 200 && responseCode < 300) {
	                    try {
	                        JSONObject result = new JSONObject(responseBodyStr);
	                        SwingUtilities.invokeLater(() -> 
	                            callback.accept(new ImageResult(true, result.getString("status"))));
	                    } catch (Exception e) {
	                        System.out.println("DEBUG - ImageService.deleteImage - Exception parsing response: " + e.getMessage());
	                        SwingUtilities.invokeLater(() -> 
	                            callback.accept(new ImageResult(false, "Failed to parse response: " + e.getMessage())));
	                    }
	                } else {
	                    String errorMsg = "Failed to delete image. Status code: " + responseCode;
	                    if (!responseBodyStr.isEmpty()) {
	                        errorMsg += " - " + responseBodyStr;
	                    }
	                    final String finalErrorMsg = errorMsg;
	                    SwingUtilities.invokeLater(() -> callback.accept(new ImageResult(false, finalErrorMsg)));
	                }
	            } catch (Exception e) {
	                System.out.println("DEBUG - ImageService.deleteImage - Exception: " + e.getMessage());
	                SwingUtilities.invokeLater(() -> 
	                    callback.accept(new ImageResult(false, "Error: " + e.getMessage())));
	            }
	        }).start();
	    }
	}