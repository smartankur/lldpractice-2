package notificationservice;

import java.time.LocalDateTime;
import java.util.UUID;

public class Notification {
    private final String id;
    private final NotificationContent content;
    private final NotificationType channel;
    private final LocalDateTime timestamp;
    private NotificationStatus status;
    
    public Notification(NotificationContent content, NotificationType channel) {
        this.id = UUID.randomUUID().toString();
        this.content = content;
        this.channel = channel;
        this.timestamp = LocalDateTime.now();
        this.status = NotificationStatus.PENDING;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public NotificationContent getContent() { return content; }
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
}