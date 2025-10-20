package filesystempackage.service;

import filesystempackage.model.*;
import filesystempackage.repository.FileSystemRepository;

import java.util.List;
import java.util.stream.Collectors;

public class FileSearcher {
    private final FileSystemRepository repository;

    public FileSearcher(FileSystemRepository repository) {
        this.repository = repository;
    }

    public List<FileSystemNode> searchByName(String pattern, User user) {
        validatePermission(user, RolePermissionMapping.READ_FILE);
        return repository.search(pattern);
    }

    public List<File> searchByType(FileType type, User user) {
        validatePermission(user, RolePermissionMapping.READ_FILE);

        return repository.search("")
                .stream()
                .filter(node -> !node.isDirectory())
                .map(node -> (File) node)
                .filter(file -> file.getType() == type)
                .collect(Collectors.toList());
    }

    public List<File> searchByContent(String content, User user) {
        validatePermission(user, RolePermissionMapping.READ_FILE);

        return repository.search("")
                .stream()
                .filter(node -> !node.isDirectory())
                .map(node -> (File) node)
                .filter(file -> file.getContent().contains(content))
                .collect(Collectors.toList());
    }

    private void validatePermission(User user, RolePermissionMapping permission) {
        if (!permission.hasPermission(user.getRole())) {
            throw new SecurityException("User does not have permission for this operation");
        }
    }
}
