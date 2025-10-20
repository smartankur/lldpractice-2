package notificationservice;

import java.time.LocalDateTime;

public abstract class ChannelDetails {
    private final String channelId;
    private final LocalDateTime createdAt;
    private boolean isActive;

    protected ChannelDetails(String channelId) {
        this.channelId = channelId;
        this.createdAt = LocalDateTime.now();
        this.isActive = true;
    }

    public abstract boolean validate();
    public abstract String getChannelType();

    // Getters and setters
    public String getChannelId() { return channelId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
