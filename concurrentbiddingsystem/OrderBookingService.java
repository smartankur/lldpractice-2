package concurrentbiddingsystem;

import lombok.Builder;
import lombok.Data;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class OrderBookingService implements OrderBook {

    @Data
    @Builder
    private static class OrderMetaDetails {
        private double amount;
        private OrderType orderType;
        private String orderId;
        private Integer quantity;
        private long orderPlacementTime;
    }

    private final ConcurrentHashMap<String, Order> orders;
    private final ConcurrentHashMap<String, PriorityBlockingQueue<OrderMetaDetails>> buyOrdersQueue;
    private final ConcurrentHashMap<String, PriorityBlockingQueue<OrderMetaDetails>> sellOrdersQueue;
    private final ConcurrentHashMap<String, ReentrantLock> symbolLocks;
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<Order>> usersToOrders;
    private final ExecutorService executor;

    public OrderBookingService() {
        orders = new ConcurrentHashMap<>();
        buyOrdersQueue = new ConcurrentHashMap<>();
        sellOrdersQueue = new ConcurrentHashMap<>();
        symbolLocks = new ConcurrentHashMap<>();  // Added
        usersToOrders = new ConcurrentHashMap<>();
        executor = Executors.newFixedThreadPool(10);
    }

    @Override
    public String submitOrder(String userId, String symbol, OrderType type, double price, int quantity) {
        ReentrantLock lock = symbolLocks.computeIfAbsent(symbol, k -> new ReentrantLock());
        lock.lock();
        try {
            var orderId = UUID.randomUUID().toString();
            var now = System.currentTimeMillis();
            var newOrder = Order.builder()
                    .orderId(orderId)
                    .userId(userId)
                    .status(OrderStatus.PENDING)
                    .quantity(quantity)
                    .filledQuantity(0)
                    .price(price)
                    .timestamp(now)
                    .symbol(symbol)
                    .type(type)
                    .build();
            var orderMetaDetails = OrderMetaDetails.builder()
                    .orderId(orderId)
                    .orderPlacementTime(now)
                    .orderType(type)
                    .quantity(quantity)
                    .amount(price)
                    .build();
            if (OrderType.BUY.equals(type)) {
                buyOrdersQueue.computeIfAbsent(symbol, a -> new PriorityBlockingQueue<>(
                        10000,
                        Comparator.comparingDouble(OrderMetaDetails::getAmount).reversed()
                                .thenComparing(OrderMetaDetails::getOrderPlacementTime)
                )).offer(orderMetaDetails);
            } else {
                sellOrdersQueue.computeIfAbsent(symbol, a -> new PriorityBlockingQueue<>(10000,
                        Comparator.comparingDouble(OrderMetaDetails::getAmount)
                                .thenComparing(OrderMetaDetails::getOrderPlacementTime))).offer(orderMetaDetails);
            }

            usersToOrders.computeIfAbsent(userId, a -> new CopyOnWriteArrayList<>()).add(newOrder);
            orders.put(orderId, newOrder);

            executeOrder(newOrder, lock);

            return orderId;
        } finally {
            lock.unlock();
        }
    }

    private void executeOrder(Order newOrder, ReentrantLock lock) {
        try {
            if (OrderType.BUY.equals(newOrder.type)) {
                var sellOrdersForSameStock = sellOrdersQueue.getOrDefault(newOrder.symbol, null);
                if (sellOrdersForSameStock != null) {
                    cleanFilledOrder(sellOrdersForSameStock);
                    var askedQuantity = newOrder.quantity - newOrder.filledQuantity;
                    var bidPrice = newOrder.price;

                    while (askedQuantity > 0 && !sellOrdersForSameStock.isEmpty()) {
                        var top = sellOrdersForSameStock.peek();
                        if (top == null || top.amount > bidPrice) {
                            break;
                        }

                        Order matchedOrder = orders.get(top.orderId);
                        if (matchedOrder == null || matchedOrder.status == OrderStatus.CANCELLED) {
                            sellOrdersForSameStock.poll();
                            continue;
                        }

                        int matchQty = Math.min(askedQuantity, top.quantity);

                        matchedOrder.filledQuantity += matchQty;
                        if (matchedOrder.filledQuantity >= matchedOrder.quantity) {
                            matchedOrder.status = OrderStatus.FILLED;
                            sellOrdersForSameStock.poll();
                        } else {
                            matchedOrder.status = OrderStatus.PARTIALLY_FILLED;
                            top.quantity -= matchQty;
                        }

                        askedQuantity -= matchQty;
                        newOrder.filledQuantity += matchQty;
                    }

                    if (newOrder.filledQuantity >= newOrder.quantity) {
                        newOrder.status = OrderStatus.FILLED;
                    } else if (newOrder.filledQuantity > 0) {
                        newOrder.status = OrderStatus.PARTIALLY_FILLED;
                    }
                }
            } else {
                var buyOrdersForSameStock = buyOrdersQueue.getOrDefault(newOrder.symbol, null);
                if (buyOrdersForSameStock != null) {
                    cleanFilledOrder(buyOrdersForSameStock);
                    var leftOverShares = newOrder.quantity - newOrder.filledQuantity;
                    var askPrice = newOrder.price;

                    while (leftOverShares > 0 && !buyOrdersForSameStock.isEmpty()) {
                        var top = buyOrdersForSameStock.peek();
                        if (top == null || top.amount < askPrice) {
                            break;
                        }

                        Order matchedOrder = orders.get(top.orderId);
                        if (matchedOrder == null || matchedOrder.status == OrderStatus.CANCELLED) {
                            buyOrdersForSameStock.poll();
                            continue;
                        }

                        int matchQty = Math.min(leftOverShares, top.quantity);

                        matchedOrder.filledQuantity += matchQty;
                        if (matchedOrder.filledQuantity >= matchedOrder.quantity) {
                            matchedOrder.status = OrderStatus.FILLED;
                            buyOrdersForSameStock.poll();
                        } else {
                            matchedOrder.status = OrderStatus.PARTIALLY_FILLED;
                            top.quantity -= matchQty;
                        }

                        leftOverShares -= matchQty;
                        newOrder.filledQuantity += matchQty;
                    }

                    if (newOrder.filledQuantity >= newOrder.quantity) {
                        newOrder.status = OrderStatus.FILLED;
                    } else if (newOrder.filledQuantity > 0) {
                        newOrder.status = OrderStatus.PARTIALLY_FILLED;
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error executing order: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void cleanFilledOrder(PriorityBlockingQueue<OrderMetaDetails> orderQueue) {
        while (!orderQueue.isEmpty()) {
            OrderMetaDetails top = orderQueue.peek();
            if (top == null) break;

            Order actualOrder = orders.get(top.orderId);
            if (actualOrder == null ||
                    actualOrder.status == OrderStatus.FILLED ||
                    actualOrder.status == OrderStatus.CANCELLED) {
                orderQueue.poll();
            } else {
                break;
            }
        }
    }

    @Override
    public boolean cancelOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) {
            return false;
        }

        ReentrantLock lock = symbolLocks.computeIfAbsent(order.symbol, k -> new ReentrantLock());
        lock.lock();
        try {
            if (order.status == OrderStatus.FILLED) {
                return false;
            }
            order.status = OrderStatus.CANCELLED;
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public OrderStatus getOrderStatus(String orderId) {
        Order order = orders.get(orderId);
        return order != null ? order.status : null;
    }

    @Override
    public Double getBestBid(String symbol) {
        PriorityBlockingQueue<OrderMetaDetails> buyOrders = buyOrdersQueue.get(symbol);
        if (buyOrders == null || buyOrders.isEmpty()) {
            return null;
        }

        ReentrantLock lock = symbolLocks.computeIfAbsent(symbol, k -> new ReentrantLock());
        lock.lock();
        try {
            cleanFilledOrder(buyOrders);
            OrderMetaDetails top = buyOrders.peek();
            return top != null ? top.amount : null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Double getBestAsk(String symbol) {
        PriorityBlockingQueue<OrderMetaDetails> sellOrders = sellOrdersQueue.get(symbol);
        if (sellOrders == null || sellOrders.isEmpty()) {
            return null;
        }

        ReentrantLock lock = symbolLocks.computeIfAbsent(symbol, k -> new ReentrantLock());
        lock.lock();
        try {
            cleanFilledOrder(sellOrders);
            OrderMetaDetails top = sellOrders.peek();
            return top != null ? top.amount : null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<Order> getUserOrders(String userId) {
        CopyOnWriteArrayList<Order> userOrders = usersToOrders.get(userId);
        return userOrders != null ? new ArrayList<>(userOrders) : new ArrayList<>();
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}