package orderdeliveryservice.service.cost;

import orderdeliveryservice.model.Delivery;
import orderdeliveryservice.model.Driver;
import orderdeliveryservice.model.Rate;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TimeCadenceBasedCostCalculator implements CostCalculator {

    private static final int PRECISION = 10;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Override
    public BigDecimal calculateCost(Driver driver, Delivery delivery) {
        long totalDurationInSeconds = delivery.getDurationInSeconds();
        Rate rate = driver.getRate();

        // Use BigDecimal division to avoid integer division precision loss
        BigDecimal durationInSeconds = new BigDecimal(totalDurationInSeconds);
        BigDecimal windowInSeconds = new BigDecimal(rate.getWindow().getSeconds());
        BigDecimal timeUnits = durationInSeconds.divide(windowInSeconds, PRECISION, ROUNDING_MODE);

        return rate.getRate().multiply(timeUnits)
                .setScale(2, ROUNDING_MODE); // Money should have 2 decimal places
    }
}