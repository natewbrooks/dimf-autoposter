package lib;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExportExcelUtil {

    public static void exportPostsToExcel(String outputPath) {
        List<PostRecord> posts = fetchPostsFromDatabase();

        if (posts.isEmpty()) {
            System.out.println("No posts to export.");
            return;
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Posts");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Post ID", "Name", "Date of Death", "Content"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        // Fill data rows
        int rowNum = 1;
        for (PostRecord post : posts) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(post.postId);
            row.createCell(1).setCellValue(post.name);
            row.createCell(2).setCellValue(post.dateOfDeath);
            row.createCell(3).setCellValue(post.content != null ? post.content : "");
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        try (FileOutputStream fileOut = new FileOutputStream(outputPath)) {
            workbook.write(fileOut);
            System.out.println("Export successful: " + outputPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<PostRecord> fetchPostsFromDatabase() {
        List<PostRecord> posts = new ArrayList<>();

        String url = "jdbc:mysql://localhost:3306/nbrooks1db";
        String username = "your_db_username";
        String password = "your_db_password";

        String query = "SELECT PostID, Name, DateOfDeath, Content FROM Posts";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int postId = rs.getInt("PostID");
                String name = rs.getString("Name");
                String dateOfDeath = rs.getString("DateOfDeath");
                String content = rs.getString("Content");

                posts.add(new PostRecord(postId, name, dateOfDeath, content));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return posts;
    }

    private static class PostRecord {
        int postId;
        String name;
        String dateOfDeath;
        String content;

        PostRecord(int postId, String name, String dateOfDeath, String content) {
            this.postId = postId;
            this.name = name;
            this.dateOfDeath = dateOfDeath;
            this.content = content;
        }
    }
}
