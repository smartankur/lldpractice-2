package filesystempackage.model;


import lombok.Getter;

@Getter
public enum FileType {
    JAVA(".java"),
    TXT(".txt"),
    PY(".py"),
    JSON(".json"),
    XML(".xml"),
    PDF(".pdf");

    private final String extension;

    FileType(String extension) {
        this.extension = extension;
    }

    public static FileType fromExtension(String ext) {
        for (FileType type : values()) {
            if (type.extension.equals(ext)) {
                return type;
            }
        }
        return TXT; // default
    }
}
