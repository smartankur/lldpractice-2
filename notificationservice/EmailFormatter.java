package notificationservice;

class EmailFormatter implements MessageFormatter {
    @Override
    public String format(NotificationContent content, User user) {
        StringBuilder html = new StringBuilder();
        html.append("<html><body>");
        html.append("<h2>Hello ").append(user.getName()).append(",</h2>");
        html.append("<p>").append(content.getMessage()).append("</p>");
        if (content.getActionUrl() != null) {
            html.append("<a href='").append(content.getActionUrl()).append("'>Click here</a>");
        }
        html.append("</body></html>");
        return html.toString();
    }
}
