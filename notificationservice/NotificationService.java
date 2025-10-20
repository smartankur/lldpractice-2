package notificationservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationService {
    private final ChannelFactory channelFactory;
    private final Map<String, List<Notification>> userNotificationHistory;
    private final List<NotificationObserver> observers;
    
    public NotificationService() {
        this.channelFactory = new ChannelFactory();
        this.userNotificationHistory = new HashMap<>();
        this.observers = new ArrayList<>();
    }
    
    public boolean send(User user, NotificationContent content) {
        NotificationPreferences prefs = user.getPreferences();
        boolean anySent = false;
        
        // Try preferred channel first
        if (prefs.getPreferredChannel() != null) {
            anySent = sendThroughChannel(prefs.getPreferredChannel(), user, content);
        }
        
        // Fallback to other enabled channels
        if (!anySent) {
            for (NotificationType type : NotificationType.values()) {
                if (prefs.isChannelEnabled(type)) {
                    ChannelDetails details = user.getChannelDetails(type);
                    if (details != null) {
                        NotificationChannel channel = channelFactory.createChannel(type);
                        channel.setChannelDetails(details);
                        if (sendThroughChannel(channel, user, content)) {
                            anySent = true;
                        }
                    }
                }
            }
        }
        
        return anySent;
    }
    
    private boolean sendThroughChannel(NotificationChannel channel, User user, 
                                      NotificationContent content) {
        ChannelDetails details = user.getChannelDetails(channel.getChannel());
        if (details != null) {
            return channel.sendMessage(details, content);
        }
        return false;
    }
    
    public void updateUserPreferences(User user) {
        // Update preferences in database
        System.out.println("Updated preferences for user: " + user.getUserId());
    }
    
    public void saveNotification(NotificationDto dto) {
        Notification notification = new Notification(dto.getContent(), 
                                                    dto.getContent().getType());
        String userId = dto.getDestinationUser().getUserId();
        
        userNotificationHistory.computeIfAbsent(userId, k -> new ArrayList<>())
                              .add(notification);
    }
    
    public List<Notification> getUserNotificationHistory(String userId) {
        return userNotificationHistory.getOrDefault(userId, new ArrayList<>());
    }
    
    public void addObserver(NotificationObserver observer) {
        observers.add(observer);
        // Add to all channels
        for (NotificationType type : NotificationType.values()) {
            NotificationChannel channel = channelFactory.createChannel(type);
            if (channel instanceof AbstractNotificationChannel) {
                ((AbstractNotificationChannel) channel).addObserver(observer);
            }
        }
    }
}
