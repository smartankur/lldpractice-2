package notificationservice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class SMSNotificationChannel extends AbstractNotificationChannel<SMSChannelDetails> {
    private static final int RATE_LIMIT_PER_DAY = 50;
    private final Map<String, Integer> rateLimitCounter = new HashMap<>();
    
    public SMSNotificationChannel() {
        this.formatter = new SMSFormatter();
    }
    
    @Override
    protected boolean deliverMessage(String message, SMSChannelDetails details) {
        // Simulate SMS sending
        System.out.println("Sending SMS to: " + details.getCountryCode() + details.getPhoneNumber());
        System.out.println("Message: " + message);
        // Integration with SMS gateway would go here
        return true;
    }
    
    @Override
    protected boolean checkRateLimit(SMSChannelDetails details) {
        String key = details.getPhoneNumber() + "_" + getCurrentDay();
        int count = rateLimitCounter.getOrDefault(key, 0);
        if (count >= RATE_LIMIT_PER_DAY) {
            return false;
        }
        rateLimitCounter.put(key, count + 1);
        return true;
    }
    
    @Override
    public boolean validateChannel(SMSChannelDetails channelDetails) {
        return channelDetails != null && channelDetails.validate();
    }
    
    @Override
    public NotificationType getChannel() {
        return NotificationType.SMS;
    }
    
    private String getCurrentDay() {
        return LocalDateTime.now().toLocalDate().toString();
    }
}