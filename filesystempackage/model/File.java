package filesystempackage.model;


import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class File extends FileSystemNode {
    private FileType type;
    private String content;  // Added content storage
    private long size;

    @Builder
    public File(String name, String path, FileType type, String content) {
        super(name, path);
        this.type = type != null ? type : determineType(name);
        this.content = content != null ? content : "";
        this.size = this.content.getBytes().length;
    }

    private FileType determineType(String name) {
        int lastDot = name.lastIndexOf(".");
        if (lastDot > 0 && lastDot < name.length() - 1) {
            String ext = name.substring(lastDot);
            return FileType.fromExtension(ext);
        }
        return FileType.TXT;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public long getSize() {
        return size;
    }

    public void updateContent(String newContent) {
        this.content = newContent;
        this.size = newContent.getBytes().length;
        this.modifiedAt = LocalDateTime.now();
    }
}
