package concurrentbiddingsystem;

import java.util.List;

public interface OrderBook {
    /**
     * Submit a new order (thread-safe)
     * @return orderId if successful, null if invalid
     */
    String submitOrder(String userId, String symbol, OrderType type, 
                      double price, int quantity);
    
    /**
     * Cancel an existing order (thread-safe)
     * @return true if cancelled, false if already filled/doesn't exist
     */
    boolean cancelOrder(String orderId);
    
    /**
     * Get current order status (thread-safe read)
     */
    OrderStatus getOrderStatus(String orderId);
    
    /**
     * Get best bid price (highest buy price)
     */
    Double getBestBid(String symbol);
    
    /**
     * Get best ask price (lowest sell price)
     */
    Double getBestAsk(String symbol);
    
    /**
     * Get all pending orders for a user
     */
    List<Order> getUserOrders(String userId);
}