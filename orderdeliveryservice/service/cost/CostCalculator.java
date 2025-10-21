package orderdeliveryservice.service.cost;

import orderdeliveryservice.model.Delivery;
import orderdeliveryservice.model.Driver;

import java.math.BigDecimal;

public interface CostCalculator {
    public BigDecimal calculateCost(Driver driver, Delivery delivery);
}
