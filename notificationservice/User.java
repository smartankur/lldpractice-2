package notificationservice;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class User {
    private final String userId;
    private String name;
    private String email;
    private NotificationPreferences preferences;
    private Map<NotificationType, ChannelDetails> channelDetailsMap;

    public User(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.channelDetailsMap = new HashMap<>();
        this.preferences = new NotificationPreferences.Builder().build();
    }

    public void subscribeToChannel(NotificationType type, ChannelDetails details) {
        channelDetailsMap.put(type, details);
    }

    public void unsubscribeFromChannel(NotificationType type) {
        channelDetailsMap.remove(type);
    }

    public ChannelDetails getChannelDetails(NotificationType type) {
        return channelDetailsMap.get(type);
    }

    // Getters and setters
    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public NotificationPreferences getPreferences() { return preferences; }
    public void setPreferences(NotificationPreferences preferences) {
        this.preferences = preferences;
    }

}

