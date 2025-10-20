package filesystempackage.service;

import filesystempackage.model.Directory;
import filesystempackage.model.FileSystemNode;
import filesystempackage.model.RolePermissionMapping;
import filesystempackage.model.User;
import filesystempackage.repository.FileSystemRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectoryManager {
    private final FileSystemRepository repository;
    private final Map<String, Directory> directoryCache;

    public DirectoryManager(FileSystemRepository repository) {
        this.repository = repository;
        this.directoryCache = new HashMap<>();
    }

    public boolean createDirectory(String path, User user) {
        validatePermission(user, RolePermissionMapping.CREATE_FILE);

        if (repository.exists(path)) {
            throw new IllegalArgumentException("Directory already exists: " + path);
        }

        // Ensure parent directory exists
        String parentPath = getParentPath(path);
        if (!parentPath.equals("/") && !repository.exists(parentPath)) {
            throw new IllegalArgumentException("Parent directory does not exist");
        }

        String name = extractName(path);
        Directory directory = Directory.create(name, path);
        directory.setOwnerRole(user.getRole());

        repository.add(directory);
        directoryCache.put(path, directory);

        return true;
    }

    public boolean removeDirectory(String path, User user) {
        validatePermission(user, RolePermissionMapping.DELETE_FILE);

        if (!repository.exists(path)) {
            throw new IllegalArgumentException("Directory does not exist: " + path);
        }

        directoryCache.remove(path);
        return repository.remove(path);
    }

    public List<FileSystemNode> listDirectory(String path, User user) {
        validatePermission(user, RolePermissionMapping.READ_FILE);
        return repository.listDirectory(path);
    }

    private void validatePermission(User user, RolePermissionMapping permission) {
        if (!permission.hasPermission(user.getRole())) {
            throw new SecurityException("User does not have permission for this operation");
        }
    }

    private String getParentPath(String path) {
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash <= 0) return "/";
        return path.substring(0, lastSlash);
    }

    private String extractName(String path) {
        int lastSlash = path.lastIndexOf('/');
        return path.substring(lastSlash + 1);
    }
}
