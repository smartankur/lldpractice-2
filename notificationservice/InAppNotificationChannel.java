package notificationservice;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class InAppNotificationChannel extends AbstractNotificationChannel<ChannelDetails> {
    private final Map<String, Queue<String>> inAppMessages = new HashMap<>();
    
    @Override
    protected boolean deliverMessage(String message, ChannelDetails details) {
        String userId = details.getChannelId();
        inAppMessages.computeIfAbsent(userId, k -> new LinkedList<>()).offer(message);
        System.out.println("In-app notification stored for user: " + userId);
        return true;
    }
    
    @Override
    protected boolean checkRateLimit(ChannelDetails details) {
        return true; // No rate limit for in-app
    }
    
    @Override
    public boolean validateChannel(ChannelDetails channelDetails) {
        return channelDetails != null;
    }
    
    @Override
    public NotificationType getChannel() {
        return NotificationType.IN_APP;
    }
    
    public Queue<String> getMessages(String userId) {
        return inAppMessages.getOrDefault(userId, new LinkedList<>());
    }
}
