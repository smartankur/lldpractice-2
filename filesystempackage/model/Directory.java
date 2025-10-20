package filesystempackage.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class Directory extends FileSystemNode {
    private Map<String, FileSystemNode> children;
    
    @Builder(builderMethodName = "directoryBuilder")
    public Directory(String name, String path, boolean isRoot) {
        super(name, path, isRoot);
        this.children = new ConcurrentHashMap<>(); // Thread-safe
    }
    
    // Static factory method for root directory
    public static Directory createRoot() {
        return directoryBuilder()
            .name("/")
            .path("/")
            .isRoot(true)
            .build();
    }
    
    // Static factory method for regular directory
    public static Directory create(String name, String path) {
        return directoryBuilder()
            .name(name)
            .path(path)
            .isRoot(false)
            .build();
    }
    
    @Override
    public boolean isDirectory() {
        return true;
    }
    
    @Override
    public long getSize() {
        long totalSize = 0;
        for (FileSystemNode child : children.values()) {
            totalSize += child.getSize();
        }
        return totalSize;
    }
    
    public void addChild(FileSystemNode node) {
        children.put(node.getName(), node);
        this.modifiedAt = LocalDateTime.now();
    }
    
    public FileSystemNode removeChild(String name) {
        FileSystemNode removed = children.remove(name);
        if (removed != null) {
            this.modifiedAt = LocalDateTime.now();
        }
        return removed;
    }
    
    public FileSystemNode getChild(String name) {
        return children.get(name);
    }
    
    public List<FileSystemNode> listChildren() {
        return new ArrayList<>(children.values());
    }
    
    public boolean hasChild(String name) {
        return children.containsKey(name);
    }
}