package loggersystem;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

public class MessageSender {
    private final Map<Level, List<Sink>> levelToSinksMap;
    private final Map<Sink, Level> sinkToLevelMap;
    private final List<LogEnricher> enrichers;
    private final ExecutorService executorService;
    
    public MessageSender(LoggerConfiguration config) {
        this.levelToSinksMap = new ConcurrentHashMap<>();
        this.sinkToLevelMap = new ConcurrentHashMap<>();
        this.enrichers = new CopyOnWriteArrayList<>(
            config.getEnrichers() != null ? config.getEnrichers() : Arrays.asList(new TimeEnricher())
        );
        this.executorService = Executors.newFixedThreadPool(10); // Configurable thread pool
        
        initializeSinks(config);
    }
    
    private void initializeSinks(LoggerConfiguration config) {
        if (config.getLevelToSinkMapping() != null) {
            config.getLevelToSinkMapping().forEach((level, sinkConfig) -> {
                Sink sink = createSink(sinkConfig);
                sink.setPriorityLevelForLevel(config.getDefaultLoggingLevel());
                
                levelToSinksMap.computeIfAbsent(level, k -> new CopyOnWriteArrayList<>())
                    .add(sink);
                sinkToLevelMap.put(sink, level);
            });
        }
    }
    
    private Sink createSink(LoggerConfiguration.SinkConfiguration config) {
        switch (config.getSinkType()) {
            case FILE:
                return new FileSink();
            case CONSOLE:
                return new ConsoleSink();
            case DATABASE:
                // return new DatabaseSink();
            default:
                throw new IllegalArgumentException("Unsupported sink type: " + config.getSinkType());
        }
    }
    
    public void sendMessage(String content, Level level, String namespace) {
        Message message = Message.builder()
            .id(UUID.randomUUID().toString())
            .content(content)
            .level(level)
            .namespace(namespace)
            .build();
        
        // Enrich message
        enrichers.forEach(enricher -> enricher.enrichMessage(message));
        
        // Get sinks for this level
        List<Sink> sinks = levelToSinksMap.get(level);
        if (sinks == null || sinks.isEmpty()) {
            System.err.println("No sinks configured for level: " + level);
            return;
        }
        
        // Async logging to avoid blocking
        for (Sink sink : sinks) {
            executorService.submit(() -> {
                try {
                    SinkDetails details = createSinkDetails(sink, message);
                    sink.logMessage(details);
                } catch (Exception e) {
                    System.err.println("Error logging message: " + e.getMessage());
                }
            });
        }
    }
    
    private SinkDetails createSinkDetails(Sink sink, Message message) {
        switch (sink.getSinkType()) {
            case FILE:
                FileSinkDetails fileDetails = FileSinkDetails.builder()
                    .location("/var/logs") // Should come from config
                    .fileName("application.log") // Should come from config
                    .build();
                fileDetails.setMessage(message);
                return fileDetails;
            case CONSOLE:
                ConsoleSinkDetails consoleDetails = ConsoleSinkDetails.builder()
                    .build();
                consoleDetails.setMessage(message);
                return consoleDetails;
            default:
                throw new IllegalArgumentException("Unsupported sink type");
        }
    }
    
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}