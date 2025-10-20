package notificationservice;

import java.util.ArrayList;
import java.util.List;

public class NotificationController {
    private final NotificationService notificationService;
    private final List<NotificationObserver> observers;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
        this.observers = new ArrayList<>();

        // Add default observer for logging
        observers.add(new LoggingObserver());
    }

    public boolean sendMessage(NotificationDto notificationDto) {
        User recipient = notificationDto.getDestinationUser();
        NotificationContent content = notificationDto.getContent();

        // Check user preferences
        if (!recipient.getPreferences().isNotificationTypeEnabled(content.getType())) {
            System.out.println("User has disabled this notification type");
            return false;
        }

        // Send through preferred channel or all enabled channels
        boolean sent = notificationService.send(recipient, content);

        if (sent) {
            // Store notification history
            notificationService.saveNotification(notificationDto);
        }

        return sent;
    }

    public void addObserver(NotificationObserver observer) {
        observers.add(observer);
        notificationService.addObserver(observer);
    }
}
