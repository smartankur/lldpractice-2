package loggersystem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FileSink implements Sink {
    private volatile Level logAboveLevel;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    @Override
    public SinkType getSinkType() {
        return SinkType.FILE;
    }
    
    @Override
    public void logMessage(SinkDetails sinkDetails) {
        if (!(sinkDetails instanceof FileSinkDetails)) {
            throw new IllegalArgumentException("Invalid sink details type");
        }
        
        FileSinkDetails fileDetails = (FileSinkDetails) sinkDetails;
        fileDetails.validate();
        
        lock.readLock().lock();
        try {
            if (!logMessageAboveLevel(fileDetails.getMessage().getLevel())) {
                return;
            }
            
            String filePath = fileDetails.getLocation() + "/" + fileDetails.getFileName();
            
            // Use try-with-resources for automatic resource management
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
                String logEntry = formatLogEntry(fileDetails.getMessage());
                writer.write(logEntry);
                writer.newLine();
                writer.flush();
            } catch (IOException e) {
                System.err.println("Failed to write to file: " + e.getMessage());
            }
        } finally {
            lock.readLock().unlock();
        }
    }
    
    private String formatLogEntry(Message message) {
        return String.format("[%s] %s - %s - %s: %s",
            message.getTime(),
            message.getLevel(),
            message.getNamespace(),
            message.getId(),
            message.getContent());
    }
    
    @Override
    public void setPriorityLevelForLevel(Level level) {
        lock.writeLock().lock();
        try {
            this.logAboveLevel = level;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean logMessageAboveLevel(Level level) {
        lock.readLock().lock();
        try {
            if (logAboveLevel == null) {
                return true; // Log all levels if not configured
            }
            // Log messages that are EQUAL or LOWER priority than configured
            // (DEBUG=0 is lowest priority, FATAL=4 is highest)
            return level.getPriority() >= logAboveLevel.getPriority();
        } finally {
            lock.readLock().unlock();
        }
    }
}
