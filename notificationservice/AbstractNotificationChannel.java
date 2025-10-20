package notificationservice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNotificationChannel<T extends ChannelDetails>
    implements NotificationChannel<T> {
    
    protected T channelDetails;
    protected List<NotificationObserver> observers = new ArrayList<>();
    protected MessageFormatter formatter;
    
    @Override
    public final boolean sendMessage(T channelDetails, NotificationContent content) {
        // Template method pattern
        if (!validateChannel(channelDetails)) {
            notifyFailure(content, "Invalid channel details");
            return false;
        }
        
        if (!checkRateLimit(channelDetails)) {
            notifyFailure(content, "Rate limit exceeded");
            return false;
        }
        
        String formattedMessage = formatMessage(content, channelDetails);
        boolean sent = deliverMessage(formattedMessage, channelDetails);
        
        if (sent) {
            logDelivery(channelDetails, content);
            notifySuccess(content);
        } else {
            notifyFailure(content, "Delivery failed");
        }
        
        return sent;
    }
    
    protected abstract boolean deliverMessage(String message, T channelDetails);
    protected abstract boolean checkRateLimit(T channelDetails);
    
    protected String formatMessage(NotificationContent content, T channelDetails) {
        return formatter != null ? 
            formatter.format(content, content.getRecipient()) : 
            content.getMessage();
    }
    
    protected void logDelivery(T channelDetails, NotificationContent content) {
        System.out.println("Delivered to " + channelDetails.getChannelType() + 
                          " at " + LocalDateTime.now());
    }
    
    public void addObserver(NotificationObserver observer) {
        observers.add(observer);
    }
    
    private void notifySuccess(NotificationContent content) {
        Notification notification = new Notification(content, getChannel());
        observers.forEach(o -> o.onNotificationSent(notification));
    }
    
    private void notifyFailure(NotificationContent content, String reason) {
        Notification notification = new Notification(content, getChannel());
        observers.forEach(o -> o.onNotificationFailed(notification, reason));
    }
    
    @Override
    public T getChannelDetails() { return channelDetails; }
    
    @Override
    public void setChannelDetails(T channelDetails) { 
        this.channelDetails = channelDetails; 
    }
}