package connectionPoolManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class ConnectionPoolTest {
    public static void main(String[] args) throws InterruptedException {
        ConnectionPool pool = new ConnectionPool(25);
        ExecutorService executor = Executors.newFixedThreadPool(10000);
        
        // Test concurrent access
        IntStream.range(0, 20).forEach(i -> {
            executor.submit(() -> {
                try {
                    Connection conn = pool.getConnection();
                    System.out.println(Thread.currentThread().getName() + 
                        " got " + conn.getConnectionId());
                    
                    Thread.sleep(10000);
                    
                    pool.releaseConnection(conn);
                    System.out.println(Thread.currentThread().getName() + 
                        " released " + conn.getConnectionId());
                        
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        });
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
        
        System.out.println("Available: " + pool.getAvailableConnections());
    }
}