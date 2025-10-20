package filesystempackage;

import filesystempackage.model.*;
import filesystempackage.repository.FileSystemRepository;
import filesystempackage.service.*;

public class FileSystemDemo {
    public static void main(String[] args) {
        // Initialize repository and services
        FileSystemRepository repository = new FileSystemRepository();
        DirectoryManager dirManager = new DirectoryManager(repository);
        FileOperationsManager fileManager = new FileOperationsManager(repository);
        FileSearcher searcher = new FileSearcher(repository);
        
        // Create users with different roles
        User admin = new User("Admin", Role.ADMIN);
        User regularUser = new User("John", Role.USER);
        User publicUser = new User("Guest", Role.PUBLIC);
        
        System.out.println("=== File System Demo ===\n");
        
        try {
            // 1. Create directory structure
            System.out.println("1. Creating directory structure:");
            dirManager.createDirectory("/documents", admin);
            dirManager.createDirectory("/documents/work", admin);
            dirManager.createDirectory("/documents/personal", admin);
            dirManager.createDirectory("/projects", admin);
            dirManager.createDirectory("/projects/java", admin);
            System.out.println("   ✓ Directories created successfully\n");
            
            // 2. Create files
            System.out.println("2. Creating files:");
            fileManager.createFile("/documents/work/report.txt", 
                "Quarterly sales report content", regularUser);
            fileManager.createFile("/documents/work/presentation.txt", 
                "Project presentation outline", regularUser);
            fileManager.createFile("/documents/personal/notes.txt", 
                "Personal notes and reminders", regularUser);
            fileManager.createFile("/projects/java/Main.java", 
                "public class Main { public static void main(String[] args) {} }", regularUser);
            fileManager.createFile("/projects/java/Utils.java", 
                "public class Utils { /* utility methods */ }", regularUser);
            System.out.println("   ✓ Files created successfully\n");
            
            // 3. List directory contents
            System.out.println("3. Listing /documents/work directory:");
            var workFiles = dirManager.listDirectory("/documents/work", publicUser);
            for (FileSystemNode node : workFiles) {
                System.out.println("   - " + node.getName() + 
                    (node.isDirectory() ? " [DIR]" : " [FILE]"));
            }
            System.out.println();
            
            // 4. Search operations
            System.out.println("4. Search operations:");
            
            // Search by name
            System.out.println("   a) Files containing 'report':");
            var searchResults = searcher.searchByName("report", publicUser);
            for (FileSystemNode node : searchResults) {
                System.out.println("      - " + node.getPath());
            }
            
            // Search by type
            System.out.println("   b) Java files:");
            var javaFiles = searcher.searchByType(FileType.JAVA, publicUser);
            for (File file : javaFiles) {
                System.out.println("      - " + file.getPath());
            }
            
            // Search by content
            System.out.println("   c) Files containing 'class':");
            var classFiles = searcher.searchByContent("class", publicUser);
            for (File file : classFiles) {
                System.out.println("      - " + file.getPath());
            }
            System.out.println();
            
            // 5. File operations
            System.out.println("5. File operations:");
            
            // Read file
            String content = fileManager.readFile("/documents/work/report.txt", publicUser);
            System.out.println("   a) Read report.txt: \"" + 
                content.substring(0, Math.min(30, content.length())) + "...\"");
            
            // Update file
            fileManager.updateFile("/documents/work/report.txt", 
                "Updated quarterly sales report with new data", regularUser);
            System.out.println("   b) Updated report.txt content");
            
            // Copy file
            fileManager.copyFile("/projects/java/Utils.java", 
                "/projects/java/UtilsBackup.java", regularUser);
            System.out.println("   c) Copied Utils.java to UtilsBackup.java");
            
            // Move file
            fileManager.moveFile("/documents/personal/notes.txt", 
                "/documents/personal/old_notes.txt", regularUser);
            System.out.println("   d) Moved notes.txt to old_notes.txt\n");
            
            // 6. Permission demonstration
            System.out.println("6. Permission demonstration:");
            try {
                fileManager.deleteFile("/documents/work/report.txt", publicUser);
            } catch (SecurityException e) {
                System.out.println("   ✗ Public user cannot delete files: " + e.getMessage());
            }
            
            try {
                fileManager.createFile("/test.txt", "test", publicUser);
            } catch (SecurityException e) {
                System.out.println("   ✗ Public user cannot create files: " + e.getMessage());
            }
            
            // Admin can delete
            fileManager.deleteFile("/documents/work/presentation.txt", admin);
            System.out.println("   ✓ Admin successfully deleted presentation.txt\n");
            
            // 7. Directory size calculation
            System.out.println("7. Directory sizes:");
            FileSystemNode projectsDir = repository.get("/projects");
            if (projectsDir instanceof Directory) {
                System.out.println("   /projects directory size: " + 
                    projectsDir.getSize() + " bytes");
            }
            
            FileSystemNode documentsDir = repository.get("/documents");
            if (documentsDir instanceof Directory) {
                System.out.println("   /documents directory size: " + 
                    documentsDir.getSize() + " bytes");
            }
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}