package loggersystem;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
public class Message {
    private final String id;
    private final String namespace;  // Added namespace
    private final String content;
    private final Level level;
    private LocalDateTime time;

    public Message(String id, String namespace, String content, Level level, LocalDateTime time) {
        this.id = id;
        this.namespace = namespace;
        this.content = content;
        this.level = level;
        this.time = time;
    }

    // Make Message immutable except for enrichment fields
    public void setTime(LocalDateTime time) {
        if (this.time == null) {  // Only set once
            this.time = time;
        }
    }
}