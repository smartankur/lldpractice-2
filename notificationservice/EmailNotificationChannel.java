package notificationservice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class EmailNotificationChannel extends AbstractNotificationChannel<EmailChannelDetails> {
    private static final int RATE_LIMIT_PER_HOUR = 100;
    private final Map<String, Integer> rateLimitCounter = new HashMap<>();

    public EmailNotificationChannel() {
        this.formatter = new EmailFormatter();
    }

    @Override
    protected boolean deliverMessage(String message, EmailChannelDetails details) {
        // Simulate email sending
        System.out.println("Sending email to: " + details.getEmailId());
        System.out.println("Message: " + message);
        // Integration with email service would go here
        return true;
    }

    @Override
    protected boolean checkRateLimit(EmailChannelDetails details) {
        String key = details.getEmailId() + "_" + getCurrentHour();
        int count = rateLimitCounter.getOrDefault(key, 0);
        if (count >= RATE_LIMIT_PER_HOUR) {
            return false;
        }
        rateLimitCounter.put(key, count + 1);
        return true;
    }

    @Override
    public boolean validateChannel(EmailChannelDetails channelDetails) {
        return channelDetails != null && channelDetails.validate();
    }

    @Override
    public NotificationType getChannel() {
        return NotificationType.EMAIL;
    }

    private String getCurrentHour() {
        return String.valueOf(LocalDateTime.now().getHour());
    }
}