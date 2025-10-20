package circuitbreaker;

import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.concurrent.atomic.AtomicInteger;

public class CircuitBreakerDemo {

    public static void main(String[] args) throws InterruptedException {

        CircuitBreakerConfig config = CircuitBreakerConfig.builder()
                .threshold(3)                           // Trip after 3 failures
                .timeoutInMs(5000)                      // Wait 5 seconds in OPEN
                .windowForFailureCount(10000)           // 10 second window
                .allowedRequestsInHalfOpen(3)           // Allow 3 test requests
                .allowedFailureRequestInHalfOpen(2)     // Allow max 2 failures in half-open
                .build();

        CircuitBreaker cb = new CircuitBreaker(config, "user123");
        RemoteService service = new RemoteService();

        System.out.println("=== Circuit Breaker Demo ===\n");

        // Scenario 1: Normal operation
        System.out.println("--- Scenario 1: Normal Operation (Successful calls) ---");
        for (int i = 0; i < 3; i++) {
            makeCall(cb, () -> service.call(true), i + 1);
        }

        // Scenario 2: Failures to trip circuit
        System.out.println("\n--- Scenario 2: Failures Trip Circuit to OPEN ---");
        for (int i = 0; i < 4; i++) {
            makeCall(cb, () -> service.call(false), i + 1);
        }

        // Scenario 3: Circuit is OPEN - requests rejected
        System.out.println("\n--- Scenario 3: Circuit OPEN - Requests Rejected ---");
        for (int i = 0; i < 3; i++) {
            makeCall(cb, () -> service.call(true), i + 1);
        }

        // Wait for timeout to transition to HALF_OPEN
        System.out.println("\n--- Waiting 6 seconds for timeout (auto-transition to HALF_OPEN) ---");
        Thread.sleep(6000);

        // Scenario 4: HALF_OPEN - Successful recovery
        System.out.println("\n--- Scenario 4: HALF_OPEN - Successful Recovery ---");
        makeCall(cb, () -> service.call(true), 1);
        makeCall(cb, () -> service.call(true), 2);
        makeCall(cb, () -> service.call(true), 3);

        // Scenario 5: Back to normal operation
        System.out.println("\n--- Scenario 5: Back to Normal Operation ---");
        for (int i = 0; i < 3; i++) {
            makeCall(cb, () -> service.call(true), i + 1);
        }

        // Scenario 6: Trip circuit again
        System.out.println("\n--- Scenario 6: Trip Circuit Again ---");
        for (int i = 0; i < 4; i++) {
            makeCall(cb, () -> service.call(false), i + 1);
        }

        // Wait for HALF_OPEN
        System.out.println("\n--- Waiting 6 seconds for HALF_OPEN ---");
        Thread.sleep(6000);

        // Scenario 7: HALF_OPEN - Failure during recovery (back to OPEN)
        System.out.println("\n--- Scenario 7: HALF_OPEN - Failure During Recovery ---");
        makeCall(cb, () -> service.call(true), 1);  // Success
        makeCall(cb, () -> service.call(false), 2); // Failure 1
        makeCall(cb, () -> service.call(false), 3); // Failure 2 - Should trip back to OPEN

        // Scenario 8: Try requests after failure in HALF_OPEN (should be blocked)
        System.out.println("\n--- Scenario 8: After HALF_OPEN Failure - Requests Blocked ---");
        for (int i = 0; i < 2; i++) {
            makeCall(cb, () -> service.call(true), i + 1);
        }

        // Wait for another HALF_OPEN transition
        System.out.println("\n--- Waiting 6 seconds for another HALF_OPEN ---");
        Thread.sleep(6000);

        // Scenario 9: HALF_OPEN - Successful recovery this time
        System.out.println("\n--- Scenario 9: HALF_OPEN - Successful Recovery (2nd Attempt) ---");
        makeCall(cb, () -> service.call(true), 1);
        makeCall(cb, () -> service.call(true), 2);
        makeCall(cb, () -> service.call(true), 3);

        // Scenario 10: Normal operation resumed
        System.out.println("\n--- Scenario 10: Normal Operation Resumed ---");
        for (int i = 0; i < 5; i++) {
            makeCall(cb, () -> service.call(true), i + 1);
        }

        // ========== CONCURRENT TESTING ==========
        System.out.println("\n\n========== CONCURRENT LOAD TESTING ==========\n");
        testConcurrentLoad(cb, service, config);

        System.out.println("\n=== Demo Complete ===");
        cb.shutdown();
    }

    /**
     * Test circuit breaker under concurrent load
     */
    private static void testConcurrentLoad(CircuitBreaker cb, RemoteService service, CircuitBreakerConfig config) throws InterruptedException {
        System.out.println("--- Scenario 11: Concurrent Load - Multiple Threads ---");

        int threadCount = 10;
        int requestsPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        AtomicInteger blockedCount = new AtomicInteger(0);

        // Submit concurrent requests
        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            CountDownLatch finalLatch = latch;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        try {
                            cb.call(() -> service.call(true));
                            successCount.incrementAndGet();
                            System.out.println("  [Thread-" + threadId + "] Request #" + (j+1) + " -> Success");
                        } catch (CircuitBreakerOpenException e) {
                            blockedCount.incrementAndGet();
                            System.out.println("  [Thread-" + threadId + "] Request #" + (j+1) + " -> Blocked");
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                            System.out.println("  [Thread-" + threadId + "] Request #" + (j+1) + " -> Failed");
                        }

                        // Small random delay (10-50ms)
                        Thread.sleep(10 + (int)(Math.random() * 40));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finalLatch.countDown();
                }
            });
        }

        // Wait for all threads to complete
        latch.await();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        System.out.println("\n[Concurrent Test Results]");
        System.out.println("  Total Requests: " + (threadCount * requestsPerThread));
        System.out.println("  Successful: " + successCount.get());
        System.out.println("  Failed: " + failureCount.get());
        System.out.println("  Blocked: " + blockedCount.get());
        System.out.println("  Final State: " + cb.getCurrentState());
        System.out.println("  Final Failure Count: " + cb.getCurrentFailureCount());

        // Scenario 12: Concurrent failures to trip circuit
        System.out.println("\n--- Scenario 12: Concurrent Failures Trip Circuit ---");
        executor = Executors.newFixedThreadPool(threadCount);
        latch = new CountDownLatch(threadCount);

        successCount.set(0);
        failureCount.set(0);
        blockedCount.set(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            CountDownLatch finalLatch1 = latch;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 2; j++) {
                        try {
                            cb.call(() -> service.call(false)); // All fail
                            successCount.incrementAndGet();
                        } catch (CircuitBreakerOpenException e) {
                            blockedCount.incrementAndGet();
                            System.out.println("  [Thread-" + threadId + "] Request #" + (j+1) + " -> Blocked");
                        } catch (Exception e) {
                            failureCount.incrementAndGet();
                            System.out.println("  [Thread-" + threadId + "] Request #" + (j+1) + " -> Failed");
                        }
                        Thread.sleep(10 + (int)(Math.random() * 20));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finalLatch1.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        System.out.println("\n[Concurrent Failure Test Results]");
        System.out.println("  Total Requests: " + (threadCount * 2));
        System.out.println("  Successful: " + successCount.get());
        System.out.println("  Failed: " + failureCount.get());
        System.out.println("  Blocked: " + blockedCount.get());
        System.out.println("  Final State: " + cb.getCurrentState());
        System.out.println("  Final Failure Count: " + cb.getCurrentFailureCount());

        // Scenario 13: Concurrent access during HALF_OPEN
        System.out.println("\n--- Scenario 13: Race Condition Test - HALF_OPEN State ---");
        System.out.println("Waiting for circuit to transition to HALF_OPEN...");
        Thread.sleep(6000);

        executor = Executors.newFixedThreadPool(20); // More threads than allowed requests
        latch = new CountDownLatch(20);

        successCount.set(0);
        failureCount.set(0);
        blockedCount.set(0);

        // Start all threads at roughly the same time to create race condition
        CountDownLatch startLatch = new CountDownLatch(1);

        for (int i = 0; i < 20; i++) {
            final int threadId = i;
            CountDownLatch finalLatch2 = latch;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for signal to start

                    try {
                        cb.call(() -> service.call(true));
                        successCount.incrementAndGet();
                        System.out.println("  [Thread-" + threadId + "] -> Success in HALF_OPEN");
                    } catch (CircuitBreakerOpenException e) {
                        blockedCount.incrementAndGet();
                        System.out.println("  [Thread-" + threadId + "] -> Blocked (limit reached)");
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                        System.out.println("  [Thread-" + threadId + "] -> Failed");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finalLatch2.countDown();
                }
            });
        }

        // Release all threads at once
        Thread.sleep(100);
        startLatch.countDown();

        latch.await();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.SECONDS);

        System.out.println("\n[HALF_OPEN Race Condition Test Results]");
        System.out.println("  Total Concurrent Requests: 20");
        System.out.println("  Allowed Requests in Config: " + config.getAllowedRequestsInHalfOpen());
        System.out.println("  Successful: " + successCount.get());
        System.out.println("  Failed: " + failureCount.get());
        System.out.println("  Blocked: " + blockedCount.get());
        System.out.println("  Final State: " + cb.getCurrentState());

        // Verify thread safety
        int allowedRequests = config.getAllowedRequestsInHalfOpen();
        int processedRequests = successCount.get() + failureCount.get();

        if (processedRequests <= allowedRequests) {
            System.out.println("  Thread Safety VERIFIED: Only " + processedRequests +
                    " requests processed (max " + allowedRequests + " allowed)");
        } else {
            System.out.println("  Thread Safety FAILED: " + processedRequests +
                    " requests processed (max " + allowedRequests + " allowed)");
        }
    }

    private static <T> void makeCall(CircuitBreaker cb, Supplier<T> supplier, int requestNum) {
        try {
            T result = cb.call(supplier);
        } catch (CircuitBreakerOpenException e) {
            System.out.println("  Request #" + requestNum + " -> Blocked: " + e.getMessage());
        } catch (Exception e) {
            // Failure already logged by circuit breaker
        }

        // Small delay between requests for readability
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

class RemoteService {
    private AtomicInteger callCount = new AtomicInteger(0);

    public String call(boolean shouldSucceed) {
        int count = callCount.incrementAndGet();
        if (!shouldSucceed) {
            throw new RuntimeException("Service unavailable (call #" + count + ")");
        }
        return "Success response (call #" + count + ")";
    }
}