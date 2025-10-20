package notificationservice;

public interface NotificationChannel<T extends ChannelDetails> {
    T getChannelDetails();
    void setChannelDetails(T channelDetails);
    boolean sendMessage(T channelDetails, NotificationContent content);
    NotificationType getChannel();
    boolean validateChannel(T channelDetails);
}
