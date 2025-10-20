package filesystempackage.service;

import filesystempackage.repository.FileSystemRepository;
import filesystempackage.model.*;
import filesystempackage.repository.FileSystemRepository;


public class FileOperationsManager {
    private final FileSystemRepository repository;

    public FileOperationsManager(FileSystemRepository repository) {
        this.repository = repository;
    }

    public File createFile(String path, String content, User user) {
        validatePermission(user, RolePermissionMapping.CREATE_FILE);

        if (repository.exists(path)) {
            throw new IllegalArgumentException("File already exists: " + path);
        }

        String name = extractName(path);
        File file = File.builder()
                .name(name)
                .path(path)
                .content(content != null ? content : "")
                .build();
        file.setOwnerRole(user.getRole());

        repository.add(file);
        return file;
    }

    public String readFile(String path, User user) {
        validatePermission(user, RolePermissionMapping.READ_FILE);

        FileSystemNode node = repository.get(path);
        if (!(node instanceof File)) {
            throw new IllegalArgumentException("Path is not a file: " + path);
        }

        return ((File) node).getContent();
    }

    public void updateFile(String path, String content, User user) {
        validatePermission(user, RolePermissionMapping.MODIFY_FILE);

        FileSystemNode node = repository.get(path);
        if (!(node instanceof File)) {
            throw new IllegalArgumentException("Path is not a file: " + path);
        }

        ((File) node).updateContent(content);
    }

    public boolean deleteFile(String path, User user) {
        validatePermission(user, RolePermissionMapping.DELETE_FILE);

        FileSystemNode node = repository.get(path);
        if (node == null || node.isDirectory()) {
            throw new IllegalArgumentException("Path is not a file: " + path);
        }

        return repository.remove(path);
    }

    public void copyFile(String sourcePath, String destPath, User user) {
        validatePermission(user, RolePermissionMapping.CREATE_FILE);

        String content = readFile(sourcePath, user);
        createFile(destPath, content, user);
    }

    public void moveFile(String sourcePath, String destPath, User user) {
        validatePermission(user, RolePermissionMapping.MODIFY_FILE);
        repository.move(sourcePath, destPath);
    }

    private void validatePermission(User user, RolePermissionMapping permission) {
        if (!permission.hasPermission(user.getRole())) {
            throw new SecurityException("User does not have permission for this operation");
        }
    }

    private String extractName(String path) {
        int lastSlash = path.lastIndexOf('/');
        return path.substring(lastSlash + 1);
    }
}
