package notificationservice;

import java.util.List;
import java.util.UUID;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        // Initialize services
        NotificationService notificationService = new NotificationService();
        NotificationController controller = new NotificationController(notificationService);
        UserController userController = new UserController(notificationService);

        // Create users
        userController.createUser("user1", "John Doe", "john@example.com");
        User john = userController.getUser("user1");

        // Set up channels for user
        john.subscribeToChannel(NotificationType.EMAIL,
                new EmailChannelDetails("john@example.com"));
        john.subscribeToChannel(NotificationType.SMS,
                new SMSChannelDetails("1234567890"));

        // Configure preferences
        NotificationPreferences preferences = new NotificationPreferences.Builder()
                .emailEnabled(true)
                .smsEnabled(true)
                .pushEnabled(false)
                .frequency(DeliveryFrequency.IMMEDIATE)
                .enableNotificationType(NotificationType.FRIEND_REQUEST, true)
                .enableNotificationType(NotificationType.SYSTEM_ALERT, false)
                .build();

        userController.addPreferences(john, preferences);

        // Send a notification
        NotificationContent content = new NotificationContent(
                "New Friend Request",
                "Jane wants to be your friend!",
                NotificationType.FRIEND_REQUEST,
                john
        );
        content.setActionUrl("https://example.com/friend-requests");
        content.setPriority(Priority.HIGH);

        NotificationDto dto = new NotificationDto();
        dto.setNotificationId(UUID.randomUUID().toString());
        dto.setDestinationUser(john);
        dto.setContent(content);

        boolean sent = controller.sendMessage(dto);
        System.out.println("Notification sent: " + sent);

        // View notification history
        List<Notification> history = notificationService.getUserNotificationHistory("user1");
        System.out.println("User has " + history.size() + " notifications");
    }
}