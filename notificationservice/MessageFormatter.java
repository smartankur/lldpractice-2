package notificationservice;

interface MessageFormatter {
    String format(NotificationContent content, User user);
}