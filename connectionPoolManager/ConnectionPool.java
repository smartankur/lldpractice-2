package connectionPoolManager;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

class ConnectionPool {
    private final Queue<Connection> availableConnections;
    private final Set<Connection> usedConnections;
    private final int poolSize;
    private final Semaphore semaphore;
    private final ReentrantLock lock;  // ✅ Lock for collections

    ConnectionPool(int poolSize) {
        this.poolSize = poolSize;
        this.availableConnections = new LinkedList<>();
        this.usedConnections = new HashSet<>();
        this.semaphore = new Semaphore(poolSize);
        this.lock = new ReentrantLock();  // ✅ Added lock

        // Initialize pool
        IntStream.range(0, poolSize)
                .forEach(i -> availableConnections.add(new Connection()));
    }

    Connection getConnection() throws InterruptedException {
        semaphore.acquire();  // Wait for available slot

        lock.lock();  // ✅ Protect collections
        try {
            Connection conn = availableConnections.poll();
            usedConnections.add(conn);
            return conn;
        } finally {
            lock.unlock();
        }
    }

    Connection getConnection(long timeoutMs) throws InterruptedException {
        // ✅ Fixed logic - check if NOT acquired
        if (!semaphore.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS)) {
            return null;  // Timeout
        }

        lock.lock();  // ✅ Protect collections
        try {
            Connection conn = availableConnections.poll();
            usedConnections.add(conn);
            return conn;
        } finally {
            lock.unlock();
        }
    }

    void releaseConnection(Connection conn) throws InterruptedException {
        if (conn == null) {
            throw new IllegalArgumentException("connectionPoolManager.Connection cannot be null");
        }

        lock.lock();  // ✅ Protect collections
        try {
            // Validate connection belongs to this pool
            if (!usedConnections.remove(conn)) {
                throw new IllegalArgumentException(
                        "connectionPoolManager.Connection does not belong to this pool or already released");
            }

            // Reset and return to available pool
            if (conn.isValid()) {
                conn.reset();
                availableConnections.add(conn);
            } else {
                // connectionPoolManager.Connection is broken, create new one
                availableConnections.add(new Connection());
            }
        } finally {
            lock.unlock();
        }

        semaphore.release();  // Signal availability
    }

    int getAvailableConnections() {
        lock.lock();  // ✅ Use lock instead of semaphore
        try {
            return availableConnections.size();
        } finally {
            lock.unlock();
        }
    }

    int getTotalConnections() {
        return poolSize;
    }

    void shutdown() {
        lock.lock();  // ✅ Use lock
        try {
            // Close all connections
            availableConnections.forEach(Connection::close);
            usedConnections.forEach(Connection::close);
            availableConnections.clear();
            usedConnections.clear();
        } finally {
            lock.unlock();
        }
    }
}