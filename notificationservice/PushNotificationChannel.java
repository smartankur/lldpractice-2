package notificationservice;

public class PushNotificationChannel extends AbstractNotificationChannel<PushChannelDetails> {
    @Override
    protected boolean deliverMessage(String message, PushChannelDetails details) {
        System.out.println("Sending push to device: " + details.getDeviceToken());
        System.out.println("Platform: " + details.getPlatform());
        System.out.println("Message: " + message);
        // Integration with FCM/APNS would go here
        return true;
    }
    
    @Override
    protected boolean checkRateLimit(PushChannelDetails details) {
        return true; // Push notifications typically don't have strict rate limits
    }
    
    @Override
    public boolean validateChannel(PushChannelDetails channelDetails) {
        return channelDetails != null && channelDetails.validate();
    }
    
    @Override
    public NotificationType getChannel() {
        return NotificationType.PUSH;
    }
}