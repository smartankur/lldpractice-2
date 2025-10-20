package notificationservice;

import java.util.UUID;

public class SMSChannelDetails extends ChannelDetails {
    private final String phoneNumber;
    private String countryCode;
    private int maxLength = 160;

    public SMSChannelDetails(String phoneNumber) {
        super(UUID.randomUUID().toString());
        this.phoneNumber = phoneNumber;
        this.countryCode = "+1"; // Default
    }

    @Override
    public boolean validate() {
        return phoneNumber != null && phoneNumber.matches("\\d{10}");
    }

    @Override
    public String getChannelType() {
        return "SMS";
    }

    public String getPhoneNumber() { return phoneNumber; }
    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
}
