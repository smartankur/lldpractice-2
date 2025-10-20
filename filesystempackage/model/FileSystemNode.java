package filesystempackage.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public abstract class FileSystemNode {
    protected String name;
    protected String path;
    protected LocalDateTime createdAt;
    protected LocalDateTime modifiedAt;
    protected Role ownerRole;

    public FileSystemNode(String name, String path, boolean isRoot) {
        // Special handling for root directory
        if (isRoot) {
            this.name = "/";
        } else {
            this.name = validateName(name);
        }
        this.path = path;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
        this.ownerRole = Role.USER; // default
    }

    public FileSystemNode(String name, String path) {
        this(name, path, false);
    }

    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        // Check for invalid characters
        if (name.contains("/") || name.contains("\\")) {
            throw new IllegalArgumentException("Name cannot contain path separators");
        }
        return name.trim();
    }

    public abstract boolean isDirectory();
    public abstract long getSize();
}