package notificationservice;

interface NotificationObserver {
    void onNotificationSent(Notification notification);
    void onNotificationFailed(Notification notification, String reason);
}