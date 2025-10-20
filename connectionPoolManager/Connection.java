package connectionPoolManager;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class Connection {
    private final String connectionId;
    private final AtomicBoolean isOpen;
    private final long createdAt;
    private volatile long lastUsedAt;
    
    public Connection() {
        this.connectionId = UUID.randomUUID().toString();
        this.isOpen = new AtomicBoolean(true);
        this.createdAt = System.currentTimeMillis();
        this.lastUsedAt = this.createdAt;
    }
    
    /**
     * Simulate executing a query
     */
    public void executeQuery(String query) {
        validateConnection();
        this.lastUsedAt = System.currentTimeMillis();
        
        // Simulate query execution time
        try {
            Thread.sleep(10 + (long)(Math.random() * 50));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("[" + connectionId + "] Executed: " + query);
    }
    
    /**
     * Check if connection is valid/open
     */
    public boolean isValid() {
        return isOpen.get();
    }
    
    /**
     * Close the connection
     */
    public void close() {
        if (isOpen.compareAndSet(true, false)) {
            System.out.println("[" + connectionId + "] connectionPoolManager.Connection closed");
        }
    }
    
    /**
     * Reopen a closed connection (for pool reuse simulation)
     */
    public void reopen() {
        if (isOpen.compareAndSet(false, true)) {
            System.out.println("[" + connectionId + "] connectionPoolManager.Connection reopened");
        }
    }
    
    /**
     * Reset connection state (useful for pool manager)
     */
    public void reset() {
        validateConnection();
        this.lastUsedAt = System.currentTimeMillis();
    }
    
    /**
     * Get connection ID for tracking
     */
    public String getConnectionId() {
        return connectionId;
    }
    
    /**
     * Get time since last use (in milliseconds)
     */
    public long getIdleTime() {
        return System.currentTimeMillis() - lastUsedAt;
    }
    
    /**
     * Get connection age (in milliseconds)
     */
    public long getAge() {
        return System.currentTimeMillis() - createdAt;
    }
    
    /**
     * Simulate connection becoming stale/broken
     */
    public void simulateFailure() {
        close();
        System.out.println("[" + connectionId + "] connectionPoolManager.Connection failed!");
    }
    
    private void validateConnection() {
        if (!isOpen.get()) {
            throw new IllegalStateException("connectionPoolManager.Connection [" + connectionId + "] is closed");
        }
    }
    
    @Override
    public String toString() {
        return "connectionPoolManager.Connection{" +
                "id='" + connectionId + '\'' +
                ", open=" + isOpen.get() +
                ", age=" + getAge() + "ms" +
                ", idle=" + getIdleTime() + "ms" +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return connectionId.equals(that.connectionId);
    }
    
    @Override
    public int hashCode() {
        return connectionId.hashCode();
    }
}