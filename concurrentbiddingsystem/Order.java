package concurrentbiddingsystem;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class Order {
    String orderId;
    String userId;
    String symbol;  // Stock symbol (e.g., "AAPL")
    OrderType type;
    double price;
    int quantity;
    int filledQuantity;
    OrderStatus status;
    long timestamp;
}