package notificationservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotificationPreferences {
    private final NotificationChannel preferredChannel;
    private final NotificationType notificationType;
    private final boolean emailEnabled;
    private final boolean smsEnabled;

    public boolean isPushEnabled() {
        return pushEnabled;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }

    public Map<NotificationType, Boolean> getTypePreferences() {
        return typePreferences;
    }

    private boolean pushEnabled;
    private Map<NotificationType, Boolean> typePreferences;
    private DeliveryFrequency frequency;

    private NotificationPreferences(Builder builder) {
        this.preferredChannel = builder.preferredChannel;
        this.notificationType = builder.notificationType;
        this.emailEnabled = builder.emailEnabled;
        this.smsEnabled = builder.smsEnabled;
        this.pushEnabled = builder.pushEnabled;
        this.typePreferences = builder.typePreferences;
        this.frequency = builder.frequency;
    }

    public static class Builder {
        private NotificationChannel preferredChannel;
        private NotificationType notificationType;
        private boolean emailEnabled = true;
        private boolean smsEnabled = false;
        private boolean pushEnabled = true;
        private Map<NotificationType, Boolean> typePreferences = new HashMap<>();
        private DeliveryFrequency frequency = DeliveryFrequency.IMMEDIATE;

        public Builder preferredChannel(NotificationChannel channel) {
            this.preferredChannel = channel;
            return this;
        }

        public Builder notificationType(NotificationType type) {
            this.notificationType = type;
            return this;
        }

        public Builder emailEnabled(boolean enabled) {
            this.emailEnabled = enabled;
            return this;
        }

        public Builder smsEnabled(boolean enabled) {
            this.smsEnabled = enabled;
            return this;
        }

        public Builder pushEnabled(boolean enabled) {
            this.pushEnabled = enabled;
            return this;
        }

        public Builder frequency(DeliveryFrequency frequency) {
            this.frequency = frequency;
            return this;
        }

        public Builder enableNotificationType(NotificationType type, boolean enabled) {
            this.typePreferences.put(type, enabled);
            return this;
        }

        public NotificationPreferences build() {
            return new NotificationPreferences(this);
        }
    }

    public boolean isChannelEnabled(NotificationType type) {
        switch (type) {
            case EMAIL: return emailEnabled;
            case SMS: return smsEnabled;
            case PUSH: return pushEnabled;
            default: return false;
        }
    }

    public boolean isNotificationTypeEnabled(NotificationType type) {
        return typePreferences.getOrDefault(type, true);
    }

    // Getters
    public NotificationChannel getPreferredChannel() { return preferredChannel; }
    public DeliveryFrequency getFrequency() { return frequency; }


}

