package lib;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.swing.JOptionPane;

/**
 * Service for exporting data to Excel format
 * Makes API calls to the backend server to generate Excel exports
 */
public class ExportExcelService {
    
    private static final String API_URL = "http://localhost:8000/api/export/excel";
    
    /**
     * Exports posts data to Excel format at the specified path
     * Uses the backend API to generate the export
     * 
     * @param outputPath The path where the Excel file should be saved
     */
    public static void exportPostsToExcel(String outputPath) {
        try {
            System.out.println("Starting Excel export to: " + outputPath);
            
            // Create URL object
            URL url = new URL(API_URL);
            
            // Open connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            // Check if the request was successful
            int status = connection.getResponseCode();
            if (status == 200) {
                // Create temp file to store the downloaded content
                File tempFile = File.createTempFile("excel-export-", ".xlsx");
                
                // Download the file
                try (InputStream in = connection.getInputStream();
                     FileOutputStream out = new FileOutputStream(tempFile)) {
                    
                    // Buffer for reading data
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    
                    // Read from input stream and write to output stream
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    
                    // Ensure all data is written
                    out.flush();
                }
                
                // Move temp file to final destination
                File outputFile = new File(outputPath);
                Files.move(tempFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                System.out.println("Export successful: " + outputPath);
                JOptionPane.showMessageDialog(null, 
                                             "Export successful: " + outputPath, 
                                             "Export Completed", 
                                             JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Handle error response
                try (InputStream errorStream = connection.getErrorStream()) {
                    String errorMessage = new String(errorStream.readAllBytes());
                    System.err.println("Export failed with status " + status + ": " + errorMessage);
                    JOptionPane.showMessageDialog(null, 
                                                 "Export failed: " + errorMessage, 
                                                 "Export Error", 
                                                 JOptionPane.ERROR_MESSAGE);
                }
            }
            
            // Close the connection
            connection.disconnect();
            
        } catch (IOException e) {
            String errorMessage = "Export failed: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                                         errorMessage, 
                                         "Export Error", 
                                         JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Alternative method that allows specifying a custom filename
     * 
     * @param outputPath The path where the Excel file should be saved
     * @param customFilename Custom filename to request from the API
     */
    public static void exportPostsToExcel(String outputPath, String customFilename) {
        try {
            System.out.println("Starting Excel export with custom filename to: " + outputPath);
            
            // Create URL object with query parameter
            URL url = new URL(API_URL + "?filename=" + customFilename);
            
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            
            int status = connection.getResponseCode();
            if (status == 200) {
                File tempFile = File.createTempFile("excel-export-", ".xlsx");
                
                try (InputStream in = connection.getInputStream();
                     FileOutputStream out = new FileOutputStream(tempFile)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    
                    out.flush();
                }
                
                File outputFile = new File(outputPath);
                Files.move(tempFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                System.out.println("Export successful: " + outputPath);
                JOptionPane.showMessageDialog(null, 
                                             "Export successful: " + outputPath, 
                                             "Export Completed", 
                                             JOptionPane.INFORMATION_MESSAGE);
            } else {
                try (InputStream errorStream = connection.getErrorStream()) {
                    String errorMessage = new String(errorStream.readAllBytes());
                    System.err.println("Export failed with status " + status + ": " + errorMessage);
                    JOptionPane.showMessageDialog(null, 
                                                 "Export failed: " + errorMessage, 
                                                 "Export Error", 
                                                 JOptionPane.ERROR_MESSAGE);
                }
            }
            
            connection.disconnect();
            
        } catch (IOException e) {
            String errorMessage = "Export failed: " + e.getMessage();
            System.err.println(errorMessage);
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, 
                                         errorMessage, 
                                         "Export Error", 
                                         JOptionPane.ERROR_MESSAGE);
        }
    }
}