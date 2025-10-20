package notificationservice;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PushChannelDetails extends ChannelDetails {
    private String deviceToken;
    private String platform; // iOS, Android, Web
    private Map<String, Object> customData;
    
    public PushChannelDetails(String deviceToken, String platform) {
        super(UUID.randomUUID().toString());
        this.deviceToken = deviceToken;
        this.platform = platform;
        this.customData = new HashMap<>();
    }
    
    @Override
    public boolean validate() {
        return deviceToken != null && !deviceToken.isEmpty() && 
               platform != null && !platform.isEmpty();
    }
    
    @Override
    public String getChannelType() {
        return "PUSH";
    }
    
    public String getDeviceToken() { return deviceToken; }
    public String getPlatform() { return platform; }
    public Map<String, Object> getCustomData() { return customData; }
}