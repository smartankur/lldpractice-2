package orderdeliveryservice;

import orderdeliveryservice.model.CalculationWindow;
import orderdeliveryservice.model.Rate;
import orderdeliveryservice.service.DeliveryPaymentSystem;
import orderdeliveryservice.service.cost.TimeCadenceBasedCostCalculator;

import java.math.BigDecimal;

public class Part1And2Test {
    /*
    public static void main(String[] args) {
        System.out.println("=== Food Delivery Payment System Test ===\n");
        
        // Initialize system
        DeliveryPaymentSystem system = new DeliveryPaymentSystem(
            new TimeCadenceBasedCostCalculator()
        );
        
        // Test Part 1: Basic functionality
        testPart1(system);
        
        System.out.println("\n" + "=".repeat(60) + "\n");
        
        // Reset for Part 2
        system = new DeliveryPaymentSystem(new TimeCadenceBasedCostCalculator());
        
        // Test Part 2: Payment functionality
        testPart2(system);
        
        System.out.println("\n" + "=".repeat(60) + "\n");
        
        // Edge cases
        system = new DeliveryPaymentSystem(new TimeCadenceBasedCostCalculator());
        testEdgeCases(system);
    }
    
    private static void testPart1(DeliveryPaymentSystem system) {
        System.out.println("--- PART 1: Basic Cost Tracking ---");
        
        // Add drivers
        Rate driver1Rate = Rate.builder()
                .rate(new BigDecimal("10.00"))
                .window(CalculationWindow.HOURLY)
                .build();
        
        Rate driver2Rate = Rate.builder()
                .rate(new BigDecimal("15.00"))
                .window(CalculationWindow.HOURLY)
                .build();
        
        system.addDriver("1", driver1Rate);
        system.addDriver("2", driver2Rate);
        System.out.println("✓ Added 2 drivers (Driver 1: $10/hr, Driver 2: $15/hr)");
        
        // Record deliveries
        long baseTime = 1609459200L; // Jan 1, 2021 00:00:00
        
        // Driver 1: 1.5 hours -> $10 * 1.5 = $15.00
        system.recordDelivery("1", baseTime, baseTime + 5400);
        System.out.println("✓ Driver 1 delivery: 1.5 hours -> Expected: $15.00");
        
        // Driver 2: 1 hour -> $15 * 1 = $15.00
        system.recordDelivery("2", baseTime + 1000, baseTime + 4600);
        System.out.println("✓ Driver 2 delivery: 1 hour -> Expected: $15.00");
        
        // Driver 1: 0.5 hours -> $10 * 0.5 = $5.00
        system.recordDelivery("1", baseTime + 6000, baseTime + 7800);
        System.out.println("✓ Driver 1 delivery: 0.5 hours -> Expected: $5.00");
        
        // Check total cost
        BigDecimal totalCost = system.getTotalCost();
        System.out.println("\n📊 Total Cost: $" + totalCost);
        System.out.println("   Expected: $35.00");
        
        boolean pass = totalCost.compareTo(new BigDecimal("35.00")) == 0;
        System.out.println("   " + (pass ? "✅ PASS" : "❌ FAIL"));
    }
    
    private static void testPart2(DeliveryPaymentSystem system) {
        System.out.println("--- PART 2: Payment Settlement ---");
        
        // Setup
        Rate rate = Rate.builder()
                .rate(new BigDecimal("10.00"))
                .window(CalculationWindow.HOURLY)
                .build();
        
        system.addDriver("1", rate);
        
        long baseTime = 1609459200L;
        
        // Record 5 deliveries at different times
        system.recordDelivery("1", baseTime, baseTime + 3600);        // Ends at +3600, cost $10
        system.recordDelivery("1", baseTime + 1000, baseTime + 4600); // Ends at +4600, cost $10
        system.recordDelivery("1", baseTime + 5000, baseTime + 8600); // Ends at +8600, cost $10
        system.recordDelivery("1", baseTime + 9000, baseTime + 10800);// Ends at +10800, cost $5
        system.recordDelivery("1", baseTime + 11000, baseTime + 14600);// Ends at +14600, cost $10
        
        System.out.println("✓ Recorded 5 deliveries");
        System.out.println("📊 Total cost: $" + system.getTotalCost() + " (Expected: $45.00)");
        
        // Payment scenario 1
        System.out.println("\n🔹 Payment 1: Up to timestamp " + (baseTime + 5000));
        system.payUpTo(baseTime + 5000);
        
        BigDecimal unpaid1 = system.getTotalCostUnpaid();
        System.out.println("   Unpaid cost: $" + unpaid1);
        System.out.println("   Expected: $35.00 (paid 2 deliveries = $20)");
        System.out.println("   " + (unpaid1.compareTo(new BigDecimal("35.00")) == 0 ? "✅ PASS" : "❌ FAIL"));
        
        // Payment scenario 2
        System.out.println("\n🔹 Payment 2: Up to timestamp " + (baseTime + 11000));
        system.payUpTo(baseTime + 11000);
        
        BigDecimal unpaid2 = system.getTotalCostUnpaid();
        System.out.println("   Unpaid cost: $" + unpaid2);
        System.out.println("   Expected: $10.00 (paid 4 total deliveries = $35)");
        System.out.println("   " + (unpaid2.compareTo(new BigDecimal("10.00")) == 0 ? "✅ PASS" : "❌ FAIL"));
        
        // Payment scenario 3 - pay all remaining
        System.out.println("\n🔹 Payment 3: Pay all remaining deliveries");
        system.payUpTo(baseTime + 20000);
        
        BigDecimal unpaid3 = system.getTotalCostUnpaid();
        System.out.println("   Unpaid cost: $" + unpaid3);
        System.out.println("   Expected: $0.00");
        System.out.println("   " + (unpaid3.compareTo(BigDecimal.ZERO) == 0 ? "✅ PASS" : "❌ FAIL"));
        
        System.out.println("\n📊 Delivery Statistics:");
        System.out.println("   - Total deliveries: " + system.getTotalDeliveriesCount());
        System.out.println("   - Paid deliveries: " + system.getPaidDeliveriesCount());
        System.out.println("   - Unpaid deliveries: " + system.getUnpaidDeliveriesCount());
    }

    private static void testEdgeCases(DeliveryPaymentSystem system) {
        System.out.println("--- EDGE CASES ---");

        Rate rate = Rate.builder()
                .rate(new BigDecimal("12.50"))
                .window(CalculationWindow.HOURLY)
                .build();

        system.addDriver("1", rate);

        long baseTime = 1609459200L;

        // Test 1: Multiple deliveries ending at SAME timestamp
        System.out.println("\n🔹 Test 1: Multiple deliveries with same end time");
        system.recordDelivery("1", baseTime, baseTime + 3600);      // Ends at 3600
        system.recordDelivery("1", baseTime + 100, baseTime + 3600); // Also ends at 3600
        system.recordDelivery("1", baseTime + 200, baseTime + 3600); // Also ends at 3600

        System.out.println("   ✓ Recorded 3 deliveries ending at same time");
        System.out.println("   Total deliveries: " + system.getTotalDeliveriesCount());
        System.out.println("   " + (system.getTotalDeliveriesCount() == 3 ?
                "✅ PASS - All 3 stored (TreeSet would fail here!)" : "❌ FAIL"));

        // Pay exactly at that timestamp
        system.payUpTo(baseTime + 3600);
        System.out.println("   Paid deliveries: " + system.getPaidDeliveriesCount());
        System.out.println("   " + (system.getPaidDeliveriesCount() == 3 ?
                "✅ PASS - All 3 paid" : "❌ FAIL"));

        // Test 2: Fractional hour calculation
        System.out.println("\n🔹 Test 2: Fractional hours (1.5 hours)");
        DeliveryPaymentSystem system2 = new DeliveryPaymentSystem(new TimeCadenceBasedCostCalculator());
        system2.addDriver("1", rate);
        system2.recordDelivery("1", baseTime, baseTime + 5400); // 1.5 hours

        BigDecimal cost = system2.getTotalCost();
        BigDecimal expected = new BigDecimal("18.75"); // 12.50 * 1.5
        System.out.println("   Cost: $" + cost);
        System.out.println("   Expected: $" + expected);
        System.out.println("   " + (cost.compareTo(expected) == 0 ?
                "✅ PASS - Correct fractional calculation" : "❌ FAIL"));

        // Test 3: Input validation
        System.out.println("\n🔹 Test 3: Input validation");
        int passCount = 0;

        try {
            system.recordDelivery("1", null, baseTime);
            System.out.println("   ❌ FAIL: Should reject null startTime");
        } catch (IllegalArgumentException e) {
            System.out.println("   ✅ PASS: Rejected null startTime");
            passCount++;
        }

        try {
            system.recordDelivery("1", baseTime + 1000, baseTime);
            System.out.println("   ❌ FAIL: Should reject startTime >= endTime");
        } catch (IllegalArgumentException e) {
            System.out.println("   ✅ PASS: Rejected startTime >= endTime");
            passCount++;
        }

        try {
            system.recordDelivery("1", baseTime, baseTime + 12000); // > 3 hours
            System.out.println("   ❌ FAIL: Should reject delivery > 3 hours");
        } catch (IllegalArgumentException e) {
            System.out.println("   ✅ PASS: Rejected delivery > 3 hours");
            passCount++;
        }

        try {
            system.recordDelivery("999", baseTime, baseTime + 3600);
            System.out.println("   ❌ FAIL: Should reject unknown driver");
        } catch (IllegalArgumentException e) {
            System.out.println("   ✅ PASS: Rejected unknown driver");
            passCount++;
        }

        try {
            system.addDriver("1", rate); // Duplicate driver
            System.out.println("   ❌ FAIL: Should reject duplicate driver");
        } catch (IllegalArgumentException e) {
            System.out.println("   ✅ PASS: Rejected duplicate driver");
            passCount++;
        }

        System.out.println("\n   Validation tests: " + passCount + "/5 passed");
    }*/
}