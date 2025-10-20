package notificationservice;

import java.util.HashMap;
import java.util.Map;

public class NotificationContent {
    private String title;
    private String message;
    private NotificationType type;
    private Map<String, String> metadata;
    private String actionUrl;
    private User recipient;
    private Priority priority;
    
    public NotificationContent(String title, String message, NotificationType type, User recipient) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.recipient = recipient;
        this.metadata = new HashMap<>();
        this.priority = Priority.NORMAL;
    }
    
    // Getters and setters
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public NotificationType getType() { return type; }
    public User getRecipient() { return recipient; }
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
}