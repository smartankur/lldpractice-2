package notificationservice;

import java.time.LocalDateTime;

public class NotificationDto {
    private String notificationId;
    private User sourceUser;
    private User destinationUser;

    public NotificationContent getContent() {
        return content;
    }

    public void setContent(NotificationContent content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    private NotificationContent content;
    private LocalDateTime createdAt;

    public String getNotificationId() { return notificationId; }
    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public User getSourceUser() { return sourceUser; }
    public void setSourceUser(User sourceUser) { this.sourceUser = sourceUser; }

    public User getDestinationUser() { return destinationUser; }
    public void setDestinationUser(User destinationUser) {
        this.destinationUser = destinationUser;
    }
}
