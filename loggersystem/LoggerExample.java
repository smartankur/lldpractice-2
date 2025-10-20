package loggersystem;

import java.util.Arrays;
import java.util.HashMap;

public class LoggerExample {
    public static void main(String[] args) {
        // Configure logger
        HashMap<Level, LoggerConfiguration.SinkConfiguration> levelMapping = new HashMap<>();
        
        levelMapping.put(Level.ERROR, LoggerConfiguration.SinkConfiguration.builder()
            .sinkType(SinkType.FILE)
            .location("/var/logs")
            .fileName("errors.log")
            .build());
            
        levelMapping.put(Level.INFO, LoggerConfiguration.SinkConfiguration.builder()
            .sinkType(SinkType.CONSOLE)
            .build());
        
        LoggerConfiguration config = LoggerConfiguration.builder()
            .timeFormat("yyyy-MM-dd HH:mm:ss")
            .defaultLoggingLevel(Level.INFO)
            .levelToSinkMapping(levelMapping)
            .enrichers(Arrays.asList(new TimeEnricher()))
            .build();
        
        MessageSender sender = new MessageSender(config);
        
        // Send messages
        sender.sendMessage("Application started", Level.INFO, "com.app.Main");
        sender.sendMessage("Database connection failed", Level.ERROR, "com.app.Database");
        sender.sendMessage("Debug information", Level.DEBUG, "com.app.Service");
        
        // Shutdown when done
        sender.shutdown();
    }
}
