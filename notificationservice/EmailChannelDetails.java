package notificationservice;

import java.util.UUID;

public class EmailChannelDetails extends ChannelDetails {
    private String emailId;
    private boolean htmlEnabled;

    public EmailChannelDetails(String emailId) {
        super(UUID.randomUUID().toString());
        this.emailId = emailId;
        this.htmlEnabled = true;
    }

    @Override
    public boolean validate() {
        return emailId != null && emailId.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    @Override
    public String getChannelType() {
        return "EMAIL";
    }

    public String getEmailId() { return emailId; }
    public void setEmailId(String emailId) { this.emailId = emailId; }
    public boolean isHtmlEnabled() { return htmlEnabled; }
    public void setHtmlEnabled(boolean htmlEnabled) { this.htmlEnabled = htmlEnabled; }
}
