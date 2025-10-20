package concurrentbiddingsystem;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class OrderBookDriver {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=".repeat(80));
        System.out.println("ORDER BOOK TESTING SUITE WITH ASSERTIONS");
        System.out.println("=".repeat(80));

        testBasicMatching();
        testPartialFills();
        testCancellation();
        testPriceTimePriority();
        testConcurrentOrders();
        testMultipleSymbols();
        stressTest();

        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST SUMMARY");
        System.out.println("=".repeat(80));
        System.out.printf("Total Tests: %d\n", totalTests);
        System.out.printf("Passed: %d\n", passedTests);
        System.out.printf("Failed: %d\n", failedTests);
        System.out.printf("Success Rate: %.1f%%\n", (passedTests * 100.0 / totalTests));
        System.out.println("=".repeat(80));
    }

    private static void assertEqual(Object expected, Object actual, String testName) {
        totalTests++;
        if (Objects.equals(expected, actual)) {
            passedTests++;
            System.out.printf("  ✓ PASS: %s (Expected: %s, Got: %s)\n", testName, expected, actual);
        } else {
            failedTests++;
            System.out.printf("  ✗ FAIL: %s (Expected: %s, Got: %s)\n", testName, expected, actual);
        }
    }

    private static void assertTrue(boolean condition, String testName) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.printf("  ✓ PASS: %s\n", testName);
        } else {
            failedTests++;
            System.out.printf("  ✗ FAIL: %s\n", testName);
        }
    }

    private static void assertNotNull(Object obj, String testName) {
        totalTests++;
        if (obj != null) {
            passedTests++;
            System.out.printf("  ✓ PASS: %s (Not null)\n", testName);
        } else {
            failedTests++;
            System.out.printf("  ✗ FAIL: %s (Was null)\n", testName);
        }
    }

    // ==================== TEST 1: BASIC MATCHING ====================

    public static void testBasicMatching() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST 1: BASIC ORDER MATCHING");
        System.out.println("=".repeat(80));

        OrderBookingService orderBook = new OrderBookingService();

        System.out.println("\nPhase 1: Submit BUY order");
        String buyOrderId = orderBook.submitOrder("user1", "AAPL", OrderType.BUY, 150.00, 100);
        assertNotNull(buyOrderId, "BUY order ID should not be null");
        assertEqual(OrderStatus.PENDING, orderBook.getOrderStatus(buyOrderId), "BUY order should be PENDING");
        assertEqual(150.00, orderBook.getBestBid("AAPL"), "Best bid should be $150.00");

        System.out.println("\nPhase 2: Submit matching SELL order");
        String sellOrderId = orderBook.submitOrder("user2", "AAPL", OrderType.SELL, 150.00, 100);
        assertNotNull(sellOrderId, "SELL order ID should not be null");

        Thread.sleep(100);

        System.out.println("\nPhase 3: Verify matching occurred");
        assertEqual(OrderStatus.FILLED, orderBook.getOrderStatus(buyOrderId), "BUY order should be FILLED");
        assertEqual(OrderStatus.FILLED, orderBook.getOrderStatus(sellOrderId), "SELL order should be FILLED");

        List<Order> user1Orders = orderBook.getUserOrders("user1");
        assertEqual(1, user1Orders.size(), "User1 should have 1 order");
        if (!user1Orders.isEmpty()) {
            Order buyOrder = user1Orders.get(0);
            assertEqual(100, buyOrder.filledQuantity, "BUY order filled quantity should be 100");
            assertEqual(100, buyOrder.quantity, "BUY order total quantity should be 100");
        }

        List<Order> user2Orders = orderBook.getUserOrders("user2");
        assertEqual(1, user2Orders.size(), "User2 should have 1 order");
        if (!user2Orders.isEmpty()) {
            Order sellOrder = user2Orders.get(0);
            assertEqual(100, sellOrder.filledQuantity, "SELL order filled quantity should be 100");
        }

        System.out.println("\nTest 1 Summary: Basic matching works correctly");
    }

    // ==================== TEST 2: PARTIAL FILLS ====================

    public static void testPartialFills() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST 2: PARTIAL FILLS");
        System.out.println("=".repeat(80));

        OrderBookingService orderBook = new OrderBookingService();

        System.out.println("\nPhase 1: Submit BUY order for 100 shares");
        String buyOrderId = orderBook.submitOrder("user1", "GOOGL", OrderType.BUY, 2800.00, 100);
        assertNotNull(buyOrderId, "BUY order should be created");

        System.out.println("\nPhase 2: Submit SELL order for 30 shares (partial match)");
        String sellOrder1 = orderBook.submitOrder("user2", "GOOGL", OrderType.SELL, 2800.00, 30);
        Thread.sleep(50);

        Order buyOrder = orderBook.getUserOrders("user1").get(0);
        assertEqual(OrderStatus.PARTIALLY_FILLED, buyOrder.status, "BUY order should be PARTIALLY_FILLED");
        assertEqual(30, buyOrder.filledQuantity, "BUY order should have 30 shares filled");

        System.out.println("\nPhase 3: Submit SELL order for 40 shares");
        String sellOrder2 = orderBook.submitOrder("user3", "GOOGL", OrderType.SELL, 2800.00, 40);
        Thread.sleep(50);

        buyOrder = orderBook.getUserOrders("user1").get(0);
        assertEqual(OrderStatus.PARTIALLY_FILLED, buyOrder.status, "BUY order should still be PARTIALLY_FILLED");
        assertEqual(70, buyOrder.filledQuantity, "BUY order should have 70 shares filled");

        System.out.println("\nPhase 4: Submit SELL order for 30 shares (complete fill)");
        String sellOrder3 = orderBook.submitOrder("user4", "GOOGL", OrderType.SELL, 2800.00, 30);
        Thread.sleep(50);

        buyOrder = orderBook.getUserOrders("user1").get(0);
        assertEqual(OrderStatus.FILLED, buyOrder.status, "BUY order should be FILLED");
        assertEqual(100, buyOrder.filledQuantity, "BUY order should have 100 shares filled");

        System.out.println("\nTest 2 Summary: Partial fills work correctly");
    }

    // ==================== TEST 3: CANCELLATION ====================

    public static void testCancellation() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST 3: ORDER CANCELLATION");
        System.out.println("=".repeat(80));

        OrderBookingService orderBook = new OrderBookingService();

        System.out.println("\nPhase 1: Submit BUY order");
        String buyOrderId = orderBook.submitOrder("user1", "MSFT", OrderType.BUY, 380.00, 50);
        assertNotNull(buyOrderId, "BUY order should be created");
        assertEqual(OrderStatus.PENDING, orderBook.getOrderStatus(buyOrderId), "Order should be PENDING");

        System.out.println("\nPhase 2: Cancel order");
        boolean cancelled = orderBook.cancelOrder(buyOrderId);
        assertTrue(cancelled, "Order should be cancelled successfully");
        assertEqual(OrderStatus.CANCELLED, orderBook.getOrderStatus(buyOrderId), "Order status should be CANCELLED");

        System.out.println("\nPhase 3: Try to match with cancelled order");
        String sellOrderId = orderBook.submitOrder("user2", "MSFT", OrderType.SELL, 380.00, 50);
        Thread.sleep(100);

        Order sellOrder = orderBook.getUserOrders("user2").get(0);
        assertEqual(OrderStatus.PENDING, sellOrder.status, "SELL order should remain PENDING (no match with cancelled)");
        assertEqual(0, sellOrder.filledQuantity, "SELL order should have 0 filled (no match)");

        System.out.println("\nPhase 4: Try to cancel already cancelled order");
        boolean cancelledAgain = orderBook.cancelOrder(buyOrderId);
        assertTrue(cancelledAgain, "Should return true even if already cancelled");

        System.out.println("\nTest 3 Summary: Cancellation prevents matching");
    }

    // ==================== TEST 4: PRICE-TIME PRIORITY ====================

    public static void testPriceTimePriority() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST 4: PRICE-TIME PRIORITY");
        System.out.println("=".repeat(80));

        OrderBookingService orderBook = new OrderBookingService();

        System.out.println("\nPhase 1: Submit SELL orders at different prices");
        String sell1 = orderBook.submitOrder("user1", "TSLA", OrderType.SELL, 250.00, 10);
        Thread.sleep(10);
        String sell2 = orderBook.submitOrder("user2", "TSLA", OrderType.SELL, 245.00, 10);
        Thread.sleep(10);
        String sell3 = orderBook.submitOrder("user3", "TSLA", OrderType.SELL, 248.00, 10);

        Double bestAsk = orderBook.getBestAsk("TSLA");
        assertEqual(245.00, bestAsk, "Best ask should be $245.00 (lowest price)");

        System.out.println("\nPhase 2: Submit BUY order at $250 (should match with $245 first)");
        String buyOrderId = orderBook.submitOrder("user4", "TSLA", OrderType.BUY, 250.00, 10);
        Thread.sleep(100);

        Order buyOrder = orderBook.getUserOrders("user4").get(0);
        assertEqual(OrderStatus.FILLED, buyOrder.status, "BUY order should be filled");

        Order sellOrder2 = orderBook.getUserOrders("user2").get(0);
        assertEqual(OrderStatus.FILLED, sellOrder2.status, "SELL at $245 should be filled (price priority)");

        Order sellOrder1 = orderBook.getUserOrders("user1").get(0);
        assertEqual(OrderStatus.PENDING, sellOrder1.status, "SELL at $250 should remain pending");

        System.out.println("\nPhase 3: Test time priority at same price");
        String sell4 = orderBook.submitOrder("user5", "TSLA", OrderType.SELL, 250.00, 10);
        Thread.sleep(20);
        String sell5 = orderBook.submitOrder("user6", "TSLA", OrderType.SELL, 250.00, 10);

        String buyOrder2 = orderBook.submitOrder("user7", "TSLA", OrderType.BUY, 250.00, 10);
        Thread.sleep(100);

        Order sellOrder4 = orderBook.getUserOrders("user5").get(0);
        assertEqual(OrderStatus.FILLED, sellOrder4.status, "Earlier SELL at $250 should match first (time priority)");

        System.out.println("\nTest 4 Summary: Price-time priority enforced correctly");
    }

    // ==================== TEST 5: CONCURRENT ORDERS ====================

    public static void testConcurrentOrders() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST 5: CONCURRENT ORDER SUBMISSION");
        System.out.println("=".repeat(80));

        OrderBookingService orderBook = new OrderBookingService();
        ExecutorService executor = Executors.newFixedThreadPool(100);

        AtomicInteger buyCount = new AtomicInteger(0);
        AtomicInteger sellCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(200);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    String orderId = orderBook.submitOrder(
                            "buyer" + finalI,
                            "AMZN",
                            OrderType.BUY,
                            170.00,
                            10
                    );
                    if (orderId != null) buyCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        for (int i = 0; i < 100; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    String orderId = orderBook.submitOrder(
                            "seller" + finalI,
                            "AMZN",
                            OrderType.SELL,
                            170.00,
                            10
                    );
                    if (orderId != null) sellCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("\nPhase 1: Verify all orders submitted");
        assertEqual(100, buyCount.get(), "Should have 100 BUY orders");
        assertEqual(100, sellCount.get(), "Should have 100 SELL orders");

        System.out.printf("  Duration: %dms\n", duration);
        System.out.printf("  Throughput: %.2f orders/sec\n", 200.0 / duration * 1000);

        Thread.sleep(200);

        System.out.println("\nPhase 2: Verify matching occurred");
        assertTrue(orderBook.getBestBid("AMZN") == null || orderBook.getBestAsk("AMZN") == null,
                "Orders should have matched (bid or ask should be empty)");

        System.out.println("\nTest 5 Summary: Concurrent submission handled correctly");
    }

    // ==================== TEST 6: MULTIPLE SYMBOLS ====================

    public static void testMultipleSymbols() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST 6: MULTIPLE SYMBOLS INDEPENDENTLY");
        System.out.println("=".repeat(80));

        OrderBookingService orderBook = new OrderBookingService();
        ExecutorService executor = Executors.newFixedThreadPool(30);

        String[] symbols = {"AAPL", "GOOGL", "MSFT"};
        ConcurrentHashMap<String, AtomicInteger> orderCounts = new ConcurrentHashMap<>();
        for (String symbol : symbols) {
            orderCounts.put(symbol, new AtomicInteger(0));
        }

        CountDownLatch latch = new CountDownLatch(90);

        System.out.println("\nPhase 1: Submit orders for 3 symbols concurrently");
        for (String symbol : symbols) {
            for (int i = 0; i < 30; i++) {
                int finalI = i;
                String finalSymbol = symbol;
                executor.submit(() -> {
                    try {
                        OrderType type = finalI % 2 == 0 ? OrderType.BUY : OrderType.SELL;
                        double price = 100.0 + (finalI % 10);
                        String orderId = orderBook.submitOrder(
                                "user" + finalI + finalSymbol,
                                finalSymbol,
                                type,
                                price,
                                10
                        );
                        if (orderId != null) {
                            orderCounts.get(finalSymbol).incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        Thread.sleep(200);

        System.out.println("\nPhase 2: Verify each symbol processed independently");
        for (String symbol : symbols) {
            assertEqual(30, orderCounts.get(symbol).get(),
                    "Should have 30 orders for " + symbol);

            Double bestBid = orderBook.getBestBid(symbol);
            Double bestAsk = orderBook.getBestAsk(symbol);
            System.out.printf("  %s - Best Bid: %s, Best Ask: %s\n",
                    symbol,
                    bestBid != null ? String.format("$%.2f", bestBid) : "None",
                    bestAsk != null ? String.format("$%.2f", bestAsk) : "None"
            );
        }

        System.out.println("\nTest 6 Summary: Multiple symbols work independently");
    }

    // ==================== TEST 7: STRESS TEST ====================

    public static void stressTest() throws InterruptedException {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TEST 7: STRESS TEST (1000 orders)");
        System.out.println("=".repeat(80));

        OrderBookingService orderBook = new OrderBookingService();
        ExecutorService executor = Executors.newFixedThreadPool(200);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(1000);

        long startTime = System.currentTimeMillis();
        Random random = new Random(12345); // Fixed seed for reproducibility

        for (int i = 0; i < 1000; i++) {
            int finalI = i;
            executor.submit(() -> {
                try {
                    OrderType type = random.nextBoolean() ? OrderType.BUY : OrderType.SELL;
                    double price = 100.0 + random.nextInt(20);
                    int quantity = 10 + random.nextInt(90);

                    String orderId = orderBook.submitOrder(
                            "user" + (finalI % 100),
                            "STRESS",
                            type,
                            price,
                            quantity
                    );

                    if (orderId != null) {
                        successCount.incrementAndGet();
                        if (random.nextInt(10) == 0) {
                            orderBook.cancelOrder(orderId);
                        }
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);

        long duration = System.currentTimeMillis() - startTime;

        System.out.println("\nPhase 1: Verify stress test results");
        assertEqual(1000, successCount.get() + failCount.get(), "Should process all 1000 orders");
        assertTrue(successCount.get() >= 900, "Should have at least 900 successful orders");

        System.out.printf("  Successful: %d\n", successCount.get());
        System.out.printf("  Failed: %d\n", failCount.get());
        System.out.printf("  Duration: %dms\n", duration);
        System.out.printf("  Throughput: %.2f orders/sec\n", 1000.0 / duration * 1000);
        System.out.printf("  Average latency: %.2fms\n", (double) duration / 1000);

        assertTrue(duration < 10000, "Should complete within 10 seconds");

        Thread.sleep(200);
        orderBook.shutdown();

        System.out.println("\nTest 7 Summary: System handles stress correctly");
    }
}