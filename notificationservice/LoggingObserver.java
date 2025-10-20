package notificationservice;

class LoggingObserver implements NotificationObserver {
    @Override
    public void onNotificationSent(Notification notification) {
        System.out.println("✓ Notification " + notification.getId() + " sent successfully");
    }
    
    @Override
    public void onNotificationFailed(Notification notification, String reason) {
        System.err.println("✗ Notification " + notification.getId() + " failed: " + reason);
    }
}
