package filesystempackage.repository;

import filesystempackage.model.Directory;
import filesystempackage.model.FileSystemNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileSystemRepository {

    private static class TrieNode {
        Map<String, TrieNode> children;
        FileSystemNode node;  // Store the actual file/directory

        TrieNode() {
            this.children = new ConcurrentHashMap<>();
        }
    }

    private final TrieNode root;

    public FileSystemRepository() {
        this.root = new TrieNode();
        // Create root directory using the static factory method
        Directory rootDir = Directory.createRoot();
        this.root.node = rootDir;
    }

    public void add(FileSystemNode node) {
        String[] pathParts = normalizePath(node.getPath()).split("/");
        TrieNode current = root;

        for (String part : pathParts) {
            if (part.isEmpty()) continue;

            current.children.putIfAbsent(part, new TrieNode());
            current = current.children.get(part);
        }

        current.node = node;

        // Update parent directory if exists
        updateParentDirectory(node);
    }

    public FileSystemNode get(String path) {
        String[] pathParts = normalizePath(path).split("/");
        TrieNode current = root;

        for (String part : pathParts) {
            if (part.isEmpty()) continue;

            if (!current.children.containsKey(part)) {
                return null;
            }
            current = current.children.get(part);
        }

        return current.node;
    }

    public boolean exists(String path) {
        return get(path) != null;
    }

    public boolean remove(String path) {
        String normalizedPath = normalizePath(path);
        String[] pathParts = normalizedPath.split("/");

        if (pathParts.length == 0) return false; // Can't remove root

        // Navigate to parent
        TrieNode parent = root;
        for (int i = 0; i < pathParts.length - 1; i++) {
            if (pathParts[i].isEmpty()) continue;
            if (!parent.children.containsKey(pathParts[i])) {
                return false;
            }
            parent = parent.children.get(pathParts[i]);
        }

        String nodeName = pathParts[pathParts.length - 1];
        TrieNode toRemove = parent.children.get(nodeName);

        if (toRemove == null) return false;

        // Check if directory is empty
        if (toRemove.node.isDirectory() && !toRemove.children.isEmpty()) {
            throw new IllegalStateException("Cannot remove non-empty directory");
        }

        parent.children.remove(nodeName);

        // Update parent directory
        if (parent.node instanceof Directory) {
            ((Directory) parent.node).removeChild(nodeName);
        }

        return true;
    }

    public List<FileSystemNode> search(String pattern) {
        List<FileSystemNode> results = new ArrayList<>();
        searchHelper(root, pattern.toLowerCase(), results);
        return results;
    }

    private void searchHelper(TrieNode node, String pattern, List<FileSystemNode> results) {
        if (node.node != null && node.node.getName().toLowerCase().contains(pattern)) {
            results.add(node.node);
        }

        for (TrieNode child : node.children.values()) {
            searchHelper(child, pattern, results);
        }
    }

    public void move(String oldPath, String newPath) {
        FileSystemNode node = get(oldPath);
        if (node == null) {
            throw new IllegalArgumentException("Source path does not exist");
        }

        if (exists(newPath)) {
            throw new IllegalArgumentException("Destination path already exists");
        }

        // Remove from old location
        remove(oldPath);

        // Update node path
        node.setPath(newPath);

        // Add to new location
        add(node);
    }

    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) return "/";

        // Ensure path starts with /
        if (!path.startsWith("/")) {
            path = "/" + path;
        }

        // Remove trailing slash except for root
        if (path.length() > 1 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // Remove duplicate slashes
        path = path.replaceAll("/+", "/");

        return path;
    }

    private void updateParentDirectory(FileSystemNode node) {
        String parentPath = getParentPath(node.getPath());
        FileSystemNode parent = get(parentPath);

        if (parent instanceof Directory) {
            ((Directory) parent).addChild(node);
        }
    }

    private String getParentPath(String path) {
        String normalized = normalizePath(path);
        int lastSlash = normalized.lastIndexOf('/');

        if (lastSlash <= 0) return "/";
        return normalized.substring(0, lastSlash);
    }

    public List<FileSystemNode> listDirectory(String path) {
        FileSystemNode node = get(path);
        if (!(node instanceof Directory)) {
            throw new IllegalArgumentException("Path is not a directory");
        }

        return ((Directory) node).listChildren();
    }
}
