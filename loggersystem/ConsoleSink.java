package loggersystem;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConsoleSink implements Sink {
    private volatile Level logAboveLevel;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    @Override
    public SinkType getSinkType() {
        return SinkType.CONSOLE;
    }
    
    @Override
    public void logMessage(SinkDetails sinkDetails) {
        if (!(sinkDetails instanceof ConsoleSinkDetails)) {
            throw new IllegalArgumentException("Invalid sink details type");
        }
        
        ConsoleSinkDetails consoleDetails = (ConsoleSinkDetails) sinkDetails;
        consoleDetails.validate();
        
        lock.readLock().lock();
        try {
            Message message = consoleDetails.getMessage();
            if (!logMessageAboveLevel(message.getLevel())) {
                return;
            }
            
            System.out.println(formatLogEntry(message));
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
                return true;
            }
            return level.getPriority() <= logAboveLevel.getPriority();
        } finally {
            lock.readLock().unlock();
        }
    }
}