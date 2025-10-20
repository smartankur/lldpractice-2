package notificationservice;

import java.util.HashMap;
import java.util.Map;

public class ChannelFactory {
    private final Map<NotificationType, NotificationChannel> channelCache = new HashMap<>();
    
    public NotificationChannel createChannel(NotificationType type) {
        if (channelCache.containsKey(type)) {
            return channelCache.get(type);
        }
        
        NotificationChannel channel;
        switch (type) {
            case EMAIL:
                channel = new EmailNotificationChannel();
                break;
            case SMS:
                channel = new SMSNotificationChannel();
                break;
            case PUSH:
                channel = new PushNotificationChannel();
                break;
            case IN_APP:
                channel = new InAppNotificationChannel();
                break;
            default:
                throw new IllegalArgumentException("Unknown channel type: " + type);
        }
        
        channelCache.put(type, channel);
        return channel;
    }
}
