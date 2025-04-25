package lib;

import java.awt.Desktop;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for handling browser actions
 */
public class BrowserUtil {
    /**
     * Opens a URL in the default web browser
     * 
     * @param url The URL to open
     * @return true if successful, false otherwise
     */
    public static boolean openUrl(String url) {
        if (url == null || url.isEmpty()) {
            System.out.println("DEBUG - BrowserUtil.openUrl - Invalid URL: " + url);
            return false;
        }
        
        try {
            // Ensure URL starts with http:// or https://
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url;
            }
            
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(new URI(url));
                System.out.println("DEBUG - BrowserUtil.openUrl - Opened URL: " + url);
                return true;
            } else {
                System.out.println("DEBUG - BrowserUtil.openUrl - Desktop browsing not supported");
                return false;
            }
        } catch (Exception e) {
            System.out.println("DEBUG - BrowserUtil.openUrl - Error opening URL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}