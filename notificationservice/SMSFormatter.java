package notificationservice;

class SMSFormatter implements MessageFormatter {
    private static final int MAX_LENGTH = 160;
    
    @Override
    public String format(NotificationContent content, User user) {
        String message = "Hi " + user.getName() + ", " + content.getMessage();
        if (message.length() > MAX_LENGTH) {
            message = message.substring(0, MAX_LENGTH - 3) + "...";
        }
        return message;
    }
}