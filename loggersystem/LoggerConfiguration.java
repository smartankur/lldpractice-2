package loggersystem;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

@Data
@Builder
public class LoggerConfiguration {
    private String timeFormat; // e.g., "yyyy-MM-dd HH:mm:ss"
    private Level defaultLoggingLevel;
    private Map<Level, SinkConfiguration> levelToSinkMapping;
    private List<LogEnricher> enrichers;
    
    @Data
    @Builder
    public static class SinkConfiguration {
        private SinkType sinkType;
        Queue<Integer> q = new ConcurrentLinkedQueue<>();
        private String location; // for file sink
        private String fileName; // for file sink
        // Add other sink-specific configurations
    }
}
