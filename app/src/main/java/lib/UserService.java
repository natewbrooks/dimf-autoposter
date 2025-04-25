package lib;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.json.JSONObject;

/**
 * Service class to handle user authentication and session management
 */
public class UserService {
    private static final String API_BASE_URL = "http://localhost:8000/api";
    
    // Current user information - static to maintain across the application
    private static int currentUserId = -1;
    private static String currentUsername = null;
    private static String currentToken = null;
    
    /**
     * User data class
     */
    public static class UserData {
        public int userId;
        public String username;
        public String email;
    }
    
    /**
     * Authentication result class
     */
    public static class AuthResult {
        public boolean success;
        public String message;
        public UserData userData;
        public String token;
        
        public AuthResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
    
    /**
     * Login with username and password
     * 
     * @param username Username
     * @param password Password
     * @param callback Callback with auth result
     */
    public static void login(String username, String password, Consumer<AuthResult> callback) {
        System.out.println("DEBUG - UserService.login - Username: " + username);
        
        SwingWorker<AuthResult, Void> loginWorker = new SwingWorker<>() {
            @Override
            protected AuthResult doInBackground() {
                try {
                    // Create JSON payload
                    JSONObject payload = new JSONObject();
                    payload.put("username", username);
                    payload.put("password", password);
                    String jsonBody = payload.toString();
                    
                    // Create connection
                    URL loginUrl = new URL(API_BASE_URL + "/auth/login/");
                    HttpURLConnection conn = (HttpURLConnection) loginUrl.openConnection();
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
                    System.out.println("DEBUG - UserService.login - Response code: " + responseCode);
                    
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
                    System.out.println("DEBUG - UserService.login - Response body: " + responseBodyStr);
                    
                    // Process response
                    if (responseCode >= 200 && responseCode < 300) {
                        JSONObject result = new JSONObject(responseBodyStr);
                        
                        // Create auth result
                        AuthResult authResult = new AuthResult(true, "Login successful");
                        authResult.token = result.getString("token");
                        
                        // Set user data
                        UserData userData = new UserData();
                        JSONObject user = result.getJSONObject("user");
                        userData.userId = user.getInt("UserID");
                        userData.username = user.getString("Username");
                        userData.email = user.optString("Email", "");
                        authResult.userData = userData;
                        
                        // Set current user
                        setCurrentUser(userData.userId, userData.username, authResult.token);
                        
                        return authResult;
                    } else {
                        String errorMsg = "Login failed";
                        if (responseBodyStr.contains("detail")) {
                            JSONObject error = new JSONObject(responseBodyStr);
                            errorMsg = error.getString("detail");
                        }
                        return new AuthResult(false, errorMsg);
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG - UserService.login - Exception: " + e.getMessage());
                    e.printStackTrace();
                    return new AuthResult(false, "Error: " + e.getMessage());
                }
            }
            
            @Override
            protected void done() {
                try {
                    AuthResult result = get();
                    SwingUtilities.invokeLater(() -> callback.accept(result));
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> 
                        callback.accept(new AuthResult(false, "Error: " + e.getMessage())));
                }
            }
        };
        
        loginWorker.execute();
    }
    
    /**
     * Register a new user
     * 
     * @param username Username
     * @param email Email
     * @param password Password
     * @param callback Callback with auth result
     */
    public static void register(String username, String email, String password, Consumer<AuthResult> callback) {
        System.out.println("DEBUG - UserService.register - Username: " + username);
        
        SwingWorker<AuthResult, Void> registerWorker = new SwingWorker<>() {
            @Override
            protected AuthResult doInBackground() {
                try {
                    // Create JSON payload
                    JSONObject payload = new JSONObject();
                    payload.put("username", username);
                    payload.put("email", email);
                    payload.put("password", password);
                    String jsonBody = payload.toString();
                    
                    // Create connection
                    URL registerUrl = new URL(API_BASE_URL + "/auth/register");  // Remove trailing slash to match backend
                    HttpURLConnection conn = (HttpURLConnection) registerUrl.openConnection();
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
                    System.out.println("DEBUG - UserService.register - Response code: " + responseCode);
                    
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
                    System.out.println("DEBUG - UserService.register - Response body: " + responseBodyStr);
                    
                    // Process response
                    if (responseCode >= 200 && responseCode < 300) {
                        JSONObject result = new JSONObject(responseBodyStr);
                        
                        // Create auth result
                        AuthResult authResult = new AuthResult(true, "Registration successful");
                        
                        // If auto-login after registration, also set token and user data
                        if (result.has("token")) {
                            authResult.token = result.getString("token");
                            
                            // Set user data
                            UserData userData = new UserData();
                            JSONObject user = result.getJSONObject("user");
                            userData.userId = user.getInt("UserID");
                            userData.username = user.getString("Username");
                            userData.email = user.optString("Email", "");
                            authResult.userData = userData;
                            
                            // Set current user
                            setCurrentUser(userData.userId, userData.username, authResult.token);
                        }
                        
                        return authResult;
                    } else {
                        String errorMsg = "Registration failed";
                        if (responseBodyStr.contains("detail")) {
                            JSONObject error = new JSONObject(responseBodyStr);
                            errorMsg = error.getString("detail");
                        }
                        return new AuthResult(false, errorMsg);
                    }
                } catch (Exception e) {
                    System.out.println("DEBUG - UserService.register - Exception: " + e.getMessage());
                    e.printStackTrace();
                    return new AuthResult(false, "Error: " + e.getMessage());
                }
            }
            
            @Override
            protected void done() {
                try {
                    AuthResult result = get();
                    SwingUtilities.invokeLater(() -> callback.accept(result));
                } catch (Exception e) {
                    e.printStackTrace();
                    SwingUtilities.invokeLater(() -> 
                        callback.accept(new AuthResult(false, "Error: " + e.getMessage())));
                }
            }
        };
        
        registerWorker.execute();
    }
    
    /**
     * Logout the current user
     */
    public static void logout() {
        // Clear session data
        currentUserId = -1;
        currentUsername = null;
        currentToken = null;
        System.out.println("DEBUG - UserService.logout - User logged out");
    }
    
    /**
     * Set the current user
     * 
     * @param userId User ID
     * @param username Username
     * @param token Auth token
     */
    private static void setCurrentUser(int userId, String username, String token) {
        currentUserId = userId;
        currentUsername = username;
        currentToken = token;
        System.out.println("DEBUG - UserService.setCurrentUser - User set: " + username + " (ID: " + userId + ")");
    }
    
    /**
     * Check if a user is currently logged in
     * 
     * @return true if logged in, false otherwise
     */
    public static boolean isLoggedIn() {
        return currentUserId > 0 && currentToken != null;
    }
    
    /**
     * Get the current user ID
     * 
     * @return Current user ID or -1 if not logged in
     */
    public static int getCurrentUserId() {
        return currentUserId;
    }
    
    /**
     * Get the current username
     * 
     * @return Current username or null if not logged in
     */
    public static String getCurrentUsername() {
        return currentUsername;
    }
    
    /**
     * Get the current auth token
     * 
     * @return Current auth token or null if not logged in
     */
    public static String getCurrentToken() {
        return currentToken;
    }
}