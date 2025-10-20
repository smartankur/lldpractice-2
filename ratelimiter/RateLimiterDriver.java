package ratelimiter;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimiterDriver {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=".repeat(80));
        System.out.println("RATE LIMITER TESTING SUITE");
        System.out.println("=".repeat(80));
        
        // Test each algorithm
        testTokenBucket();
        testFixedWindow();
        testSlidingWindow();
        testLeakyBucket();
        
        // Stress test
        stressTest();
        
        // Concurrent test
        concurrentTest();
    }

    // ==================== TOKEN BUCKET TEST ====================
    
    public static void testTokenBucket() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST 1: TOKEN BUCKET ALGORITHM");
        System.out.println("=".repeat(80));
        
        RateLimiterConfig config = RateLimiterConfig.builder()
            .bucketCapacity(5)
            .refillRate(2.0)  // 2 tokens per second
            .build();
        
        TokenBucketAlgorithm limiter = new TokenBucketAlgorithm(config);
        
        System.out.println("Config: Capacity=5, RefillRate=2 tokens/sec\n");
        
        // Test burst capability
        System.out.println("Phase 1: Burst test (5 requests immediately)");
        for (int i = 1; i <= 5; i++) {
            boolean allowed = limiter.allowRequest("user1");
            System.out.printf("  Request %d: %s (Remaining: %d)\n", 
                i, allowed ? "ALLOWED" : "DENIED", 
                limiter.getRemainingRequests("user1"));
        }
        
        // Should deny 6th
        System.out.println("\nPhase 2: Attempt 6th request (should deny)");
        boolean allowed = limiter.allowRequest("user1");
        System.out.printf("  Request 6: %s (Remaining: %d)\n", 
            allowed ? "ALLOWED" : "DENIED", 
            limiter.getRemainingRequests("user1"));
        
        // Wait for refill
        System.out.println("\nPhase 3: Wait 1 second for refill...");
        Thread.sleep(1100);
        System.out.printf("  After refill - Remaining: %d\n", 
            limiter.getRemainingRequests("user1"));
        
        // Should allow 2 more
        System.out.println("\nPhase 4: Try 2 more requests");
        for (int i = 7; i <= 8; i++) {
            allowed = limiter.allowRequest("user1");
            System.out.printf("  Request %d: %s (Remaining: %d)\n", 
                i, allowed ? "ALLOWED" : "DENIED", 
                limiter.getRemainingRequests("user1"));
        }
        
        System.out.println("\n✓ Token Bucket test completed");
    }

    // ==================== FIXED WINDOW TEST ====================
    
    public static void testFixedWindow() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST 2: FIXED WINDOW COUNTER ALGORITHM");
        System.out.println("=".repeat(80));
        
        RateLimiterConfig config = RateLimiterConfig.builder()
            .maxRequests(10)
            .windowSizeMs(5000)  // 5 second window
            .build();
        
        FixedWindowCounterAlgorithm limiter = new FixedWindowCounterAlgorithm(config);
        
        System.out.println("Config: MaxRequests=10, Window=5 seconds\n");
        
        // Make 10 requests
        System.out.println("Phase 1: Make 10 requests");
        for (int i = 1; i <= 10; i++) {
            boolean allowed = limiter.allowRequest("user1");
            System.out.printf("  Request %d: %s (Used: %d/%d, Remaining: %d)\n", 
                i, allowed ? "ALLOWED" : "DENIED",
                limiter.getCurrentCount("user1"),
                10,
                limiter.getRemainingRequests("user1"));
        }
        
        // Try 11th (should deny)
        System.out.println("\nPhase 2: Try 11th request (should deny)");
        boolean allowed = limiter.allowRequest("user1");
        System.out.printf("  Request 11: %s (Used: %d/%d)\n", 
            allowed ? "ALLOWED" : "DENIED",
            limiter.getCurrentCount("user1"), 10);
        
        // Wait for window reset
        System.out.println("\nPhase 3: Wait for window reset (5 seconds)...");
        Thread.sleep(5100);
        
        System.out.printf("  After reset - Used: %d/%d, Remaining: %d\n",
            limiter.getCurrentCount("user1"), 10,
            limiter.getRemainingRequests("user1"));
        
        // Should allow again
        System.out.println("\nPhase 4: Try request after reset");
        allowed = limiter.allowRequest("user1");
        System.out.printf("  Request 12: %s (Used: %d/%d)\n", 
            allowed ? "ALLOWED" : "DENIED",
            limiter.getCurrentCount("user1"), 10);
        
        limiter.shutdown();
        System.out.println("\n✓ Fixed Window test completed");
    }

    // ==================== SLIDING WINDOW TEST ====================
    
    public static void testSlidingWindow() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST 3: SLIDING WINDOW COUNTER ALGORITHM");
        System.out.println("=".repeat(80));
        
        RateLimiterConfig config = RateLimiterConfig.builder()
            .maxRequests(10)
            .windowSizeMs(10000)  // 10 second window
            .build();
        
        SlidingWindowCounterAlgorithm limiter = new SlidingWindowCounterAlgorithm(config);
        
        System.out.println("Config: MaxRequests=10, Window=10 seconds\n");
        
        // Make 8 requests
        System.out.println("Phase 1: Make 8 requests at t=0");
        for (int i = 1; i <= 8; i++) {
            limiter.allowRequest("user1");
        }
        System.out.printf("  Used: %d/%d\n", limiter.getCurrentCount("user1"), 10);
        
        // Wait 5 seconds (middle of window)
        System.out.println("\nPhase 2: Wait 5 seconds (middle of window)...");
        Thread.sleep(5000);
        
        // Try 2 more
        System.out.println("\nPhase 3: Try 2 more requests at t=5s");
        for (int i = 9; i <= 10; i++) {
            boolean allowed = limiter.allowRequest("user1");
            System.out.printf("  Request %d: %s (Weighted count: %d)\n", 
                i, allowed ? "ALLOWED" : "DENIED",
                limiter.getCurrentCount("user1"));
        }
        
        // Try 11th (should deny due to weighted calculation)
        System.out.println("\nPhase 4: Try 11th request");
        boolean allowed = limiter.allowRequest("user1");
        System.out.printf("  Request 11: %s (Weighted count: %d)\n", 
            allowed ? "ALLOWED" : "DENIED",
            limiter.getCurrentCount("user1"));
        
        System.out.println("\n✓ Sliding Window test completed");
    }

    // ==================== LEAKY BUCKET TEST ====================
    
    public static void testLeakyBucket() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST 4: LEAKY BUCKET ALGORITHM");
        System.out.println("=".repeat(80));
        
        RateLimiterConfig config = RateLimiterConfig.builder()
            .maxRequests(2)  // Leak 2 per second
            .bucketCapacity(5)
            .build();
        
        LeakyBucketAlgorithm limiter = new LeakyBucketAlgorithm(config);
        
        System.out.println("Config: LeakRate=2/sec, Capacity=5\n");
        
        // Fill bucket
        System.out.println("Phase 1: Fill bucket (5 requests)");
        for (int i = 1; i <= 5; i++) {
            boolean allowed = limiter.allowRequest("user1");
            System.out.printf("  Request %d: %s (Queue size: %d/%d)\n", 
                i, allowed ? "ALLOWED" : "DENIED",
                limiter.getCurrentCount("user1"), 5);
        }
        
        // Try 6th (should overflow)
        System.out.println("\nPhase 2: Try 6th request (overflow)");
        boolean allowed = limiter.allowRequest("user1");
        System.out.printf("  Request 6: %s (Queue size: %d/%d)\n", 
            allowed ? "ALLOWED" : "DENIED",
            limiter.getCurrentCount("user1"), 5);
        
        // Wait for leak
        System.out.println("\nPhase 3: Wait 1 second for leak (2 items leaked)...");
        Thread.sleep(1100);
        System.out.printf("  After leak - Queue size: %d/%d\n",
            limiter.getCurrentCount("user1"), 5);
        
        // Should allow 2 more
        System.out.println("\nPhase 4: Add 2 more requests");
        for (int i = 7; i <= 8; i++) {
            allowed = limiter.allowRequest("user1");
            System.out.printf("  Request %d: %s (Queue size: %d/%d)\n", 
                i, allowed ? "ALLOWED" : "DENIED",
                limiter.getCurrentCount("user1"), 5);
        }
        
        limiter.shutdown();
        System.out.println("\n✓ Leaky Bucket test completed");
    }

    // ==================== STRESS TEST ====================
    
    public static void stressTest() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST 5: STRESS TEST (1000 requests, 100 threads)");
        System.out.println("=".repeat(80));
        
        RateLimiterConfig config = RateLimiterConfig.builder()
            .maxRequests(100)
            .windowSizeMs(1000)
            .build();
        
        FixedWindowCounterAlgorithm limiter = new FixedWindowCounterAlgorithm(config);
        
        ExecutorService executor = Executors.newFixedThreadPool(100);
        AtomicInteger allowed = new AtomicInteger(0);
        AtomicInteger denied = new AtomicInteger(0);
        
        long startTime = System.currentTimeMillis();
        
        // Submit 1000 requests
        CountDownLatch latch = new CountDownLatch(1000);
        for (int i = 0; i < 1000; i++) {
            executor.submit(() -> {
                try {
                    if (limiter.allowRequest("user1")) {
                        allowed.incrementAndGet();
                    } else {
                        denied.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();
        long duration = System.currentTimeMillis() - startTime;
        
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.printf("\nResults:\n");
        System.out.printf("  Total requests: 1000\n");
        System.out.printf("  Allowed: %d\n", allowed.get());
        System.out.printf("  Denied: %d\n", denied.get());
        System.out.printf("  Duration: %dms\n", duration);
        System.out.printf("  Throughput: %.2f req/sec\n", 1000.0 / duration * 1000);
        System.out.printf("  Accuracy: %.1f%% (expected ~100 allowed)\n", 
            (100.0 - Math.abs(100 - allowed.get())));
        
        limiter.shutdown();
        System.out.println("\n✓ Stress test completed");
    }

    // ==================== CONCURRENT TEST ====================
    
    public static void concurrentTest() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST 6: CONCURRENT USERS TEST (3 users, 50 requests each)");
        System.out.println("=".repeat(80));
        
        RateLimiterConfig config = RateLimiterConfig.builder()
            .maxRequests(20)
            .windowSizeMs(2000)
            .build();
        
        FixedWindowCounterAlgorithm limiter = new FixedWindowCounterAlgorithm(config);
        
        ExecutorService executor = Executors.newFixedThreadPool(30);
        
        String[] users = {"alice", "bob", "charlie"};
        ConcurrentHashMap<String, AtomicInteger> results = new ConcurrentHashMap<>();
        for (String user : users) {
            results.put(user, new AtomicInteger(0));
        }
        
        CountDownLatch latch = new CountDownLatch(150);
        
        // Each user makes 50 requests
        for (String user : users) {
            for (int i = 0; i < 50; i++) {
                executor.submit(() -> {
                    try {
                        if (limiter.allowRequest(user)) {
                            results.get(user).incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }
        
        latch.await();
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        
        System.out.println("\nResults per user:");
        for (String user : users) {
            int allowed = results.get(user).get();
            System.out.printf("  %s: %d/%d allowed (%.1f%%)\n", 
                user, allowed, 50, allowed / 50.0 * 100);
        }
        
        limiter.shutdown();
        System.out.println("\n✓ Concurrent test completed");
    }

    // ==================== COMPARISON TEST ====================
    
    public static void comparisonTest() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST 7: ALGORITHM COMPARISON");
        System.out.println("=".repeat(80));
        
        System.out.println("\nScenario: 150 requests in first second, 50 in second second");
        System.out.println("Limit: 100 requests per minute\n");
        
        // Test each algorithm
        testAlgorithm("Token Bucket", new TokenBucketAlgorithm(
            RateLimiterConfig.builder().bucketCapacity(100).refillRate(100.0/60).build()
        ));
        
        testAlgorithm("Fixed Window", new FixedWindowCounterAlgorithm(
            RateLimiterConfig.builder().maxRequests(100).windowSizeMs(60000).build()
        ));
        
        testAlgorithm("Sliding Window", new SlidingWindowCounterAlgorithm(
            RateLimiterConfig.builder().maxRequests(100).windowSizeMs(60000).build()
        ));
        
        testAlgorithm("Leaky Bucket", new LeakyBucketAlgorithm(
            RateLimiterConfig.builder().maxRequests(100).bucketCapacity(100).build()
        ));
    }
    
    private static void testAlgorithm(String name, RateLimiter limiter) 
            throws InterruptedException {
        System.out.printf("Testing %s:\n", name);
        
        int allowed1 = 0, allowed2 = 0;
        
        // 150 requests in first second
        for (int i = 0; i < 150; i++) {
            if (limiter.allowRequest("user1")) {
                allowed1++;
            }
        }
        
        Thread.sleep(1000);
        
        // 50 requests in second second
        for (int i = 0; i < 50; i++) {
            if (limiter.allowRequest("user1")) {
                allowed2++;
            }
        }
        
        System.out.printf("  First second: %d/150 allowed\n", allowed1);
        System.out.printf("  Second second: %d/50 allowed\n", allowed2);
        System.out.printf("  Total: %d/200 allowed\n\n", allowed1 + allowed2);
    }
}