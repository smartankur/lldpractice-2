// ============================================================================
// PROBLEM STATEMENT
// ============================================================================
/*
 * CURRENCY EXCHANGE RATE SERVICE WITH INTELLIGENT CACHING
 * 
 * CONTEXT:
 * You are building a financial services platform that needs to provide 
 * real-time currency exchange rates to users. The system should support
 * currency conversion between multiple currencies with high performance
 * and reliability.
 * 
 * PROBLEM DESCRIPTION:
 * Design and implement a Currency Exchange Rate Service that:
 * 
 * 1. Fetches exchange rates from an external API (simulated)
 * 2. Caches exchange rates with TTL (Time To Live) to reduce API calls
 * 3. Supports currency conversion between any two currencies
 * 4. Handles cache invalidation and automatic refresh
 * 5. Provides transaction history tracking
 * 6. Implements rate manipulation for calculations (fees, margins)
 * 
 * FUNCTIONAL REQUIREMENTS:
 * - Get exchange rate between any two currencies (e.g., EUR to GBP)
 * - Convert amount from one currency to another
 * - Cache rates with configurable expiration (default: 5 minutes)
 * - Auto-refresh expired rates
 * - Support for multiple base currencies
 * - Add transaction fees/margins to rates
 * - Track conversion history
 * 
 * NON-FUNCTIONAL REQUIREMENTS:
 * - Low latency: < 100ms for cached rates
 * - High availability: Handle API failures gracefully
 * - Thread-safe operations
 * - Memory efficient caching
 * 
 * TECHNICAL CONSTRAINTS:
 * - Use Java 8+
 * - Implement proper OOP design patterns
 * - Use appropriate data structures
 * - Handle edge cases (null values, invalid currencies, etc.)
 */

// ============================================================================
// IMPLEMENTATION
// ============================================================================

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// ============================================================================
// 1. DOMAIN MODELS
// ============================================================================

class ExchangeRate {
    private final String fromCurrency;
    private final String toCurrency;
    private final BigDecimal rate;
    private final LocalDateTime timestamp;
    private final LocalDateTime expiresAt;
    
    public ExchangeRate(String fromCurrency, String toCurrency, BigDecimal rate, 
                       int ttlMinutes) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.rate = rate;
        this.timestamp = LocalDateTime.now();
        this.expiresAt = timestamp.plusMinutes(ttlMinutes);
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public String getFromCurrency() { return fromCurrency; }
    public String getToCurrency() { return toCurrency; }
    public BigDecimal getRate() { return rate; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("%s/%s: %.4f (expires: %s)", 
            fromCurrency, toCurrency, rate, expiresAt);
    }
}

class ConversionResult {
    private final String fromCurrency;
    private final String toCurrency;
    private final BigDecimal fromAmount;
    private final BigDecimal toAmount;
    private final BigDecimal rate;
    private final BigDecimal fee;
    private final LocalDateTime timestamp;
    
    public ConversionResult(String fromCurrency, String toCurrency, 
                           BigDecimal fromAmount, BigDecimal toAmount, 
                           BigDecimal rate, BigDecimal fee) {
        this.fromCurrency = fromCurrency;
        this.toCurrency = toCurrency;
        this.fromAmount = fromAmount;
        this.toAmount = toAmount;
        this.rate = rate;
        this.fee = fee;
        this.timestamp = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return String.format("%.2f %s → %.2f %s (Rate: %.4f, Fee: %.2f%%)", 
            fromAmount, fromCurrency, toAmount, toCurrency, rate, fee);
    }
    
    public BigDecimal getToAmount() { return toAmount; }
}

// ============================================================================
// 2. CACHE IMPLEMENTATION
// ============================================================================

class ExchangeRateCache {
    private final Map<String, ExchangeRate> cache;
    private final int defaultTTLMinutes;
    
    public ExchangeRateCache(int defaultTTLMinutes) {
        this.cache = new ConcurrentHashMap<>();
        this.defaultTTLMinutes = defaultTTLMinutes;
    }
    
    public void put(String fromCurrency, String toCurrency, BigDecimal rate) {
        String key = getCacheKey(fromCurrency, toCurrency);
        ExchangeRate exchangeRate = new ExchangeRate(fromCurrency, toCurrency, 
                                                     rate, defaultTTLMinutes);
        cache.put(key, exchangeRate);
    }
    
    public Optional<ExchangeRate> get(String fromCurrency, String toCurrency) {
        String key = getCacheKey(fromCurrency, toCurrency);
        ExchangeRate rate = cache.get(key);
        
        if (rate == null) {
            return Optional.empty();
        }
        
        if (rate.isExpired()) {
            cache.remove(key);
            return Optional.empty();
        }
        
        return Optional.of(rate);
    }
    
    public void invalidate(String fromCurrency, String toCurrency) {
        String key = getCacheKey(fromCurrency, toCurrency);
        cache.remove(key);
    }
    
    public void clear() {
        cache.clear();
    }
    
    public int size() {
        return cache.size();
    }
    
    private String getCacheKey(String fromCurrency, String toCurrency) {
        return fromCurrency + "_" + toCurrency;
    }
}

// ============================================================================
// 3. EXTERNAL API SIMULATOR
// ============================================================================

interface ExchangeRateAPI {
    BigDecimal fetchRate(String fromCurrency, String toCurrency);
}

class MockExchangeRateAPI implements ExchangeRateAPI {
    private final Map<String, BigDecimal> baseRates;
    
    public MockExchangeRateAPI() {
        // Initialize with some realistic rates (base: USD)
        baseRates = new HashMap<>();
        baseRates.put("USD", new BigDecimal("1.0"));
        baseRates.put("EUR", new BigDecimal("0.85"));
        baseRates.put("GBP", new BigDecimal("0.73"));
        baseRates.put("JPY", new BigDecimal("110.50"));
        baseRates.put("CAD", new BigDecimal("1.25"));
        baseRates.put("AUD", new BigDecimal("1.35"));
        baseRates.put("CHF", new BigDecimal("0.92"));
        baseRates.put("INR", new BigDecimal("74.50"));
    }
    
    @Override
    public BigDecimal fetchRate(String fromCurrency, String toCurrency) {
        // Simulate API call delay
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        if (!baseRates.containsKey(fromCurrency) || 
            !baseRates.containsKey(toCurrency)) {
            throw new IllegalArgumentException("Unsupported currency");
        }
        
        // Calculate cross rate through USD
        BigDecimal fromRate = baseRates.get(fromCurrency);
        BigDecimal toRate = baseRates.get(toCurrency);
        
        return toRate.divide(fromRate, 6, RoundingMode.HALF_UP);
    }
}

// ============================================================================
// 4. MAIN SERVICE
// ============================================================================

class CurrencyExchangeService {
    private final ExchangeRateCache cache;
    private final ExchangeRateAPI api;
    private final List<ConversionResult> transactionHistory;
    private BigDecimal feePercentage;
    
    public CurrencyExchangeService(ExchangeRateAPI api, int cacheTTLMinutes) {
        this.api = api;
        this.cache = new ExchangeRateCache(cacheTTLMinutes);
        this.transactionHistory = new ArrayList<>();
        this.feePercentage = BigDecimal.ZERO;
    }
    
    public void setFeePercentage(BigDecimal feePercentage) {
        this.feePercentage = feePercentage;
    }
    
    /**
     * Get exchange rate with caching
     */
    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        // Check if same currency
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }
        
        // Try to get from cache
        Optional<ExchangeRate> cachedRate = cache.get(fromCurrency, toCurrency);
        if (cachedRate.isPresent()) {
            System.out.println("✓ Cache HIT: " + cachedRate.get());
            return cachedRate.get().getRate();
        }
        
        // Cache miss - fetch from API
        System.out.println("✗ Cache MISS: Fetching " + fromCurrency + "/" + toCurrency);
        BigDecimal rate = api.fetchRate(fromCurrency, toCurrency);
        cache.put(fromCurrency, toCurrency, rate);
        
        return rate;
    }
    
    /**
     * Convert amount with fee manipulation
     */
    public ConversionResult convert(String fromCurrency, String toCurrency, 
                                   BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        
        BigDecimal rate = getExchangeRate(fromCurrency, toCurrency);
        
        // Apply fee (reduce rate by fee percentage)
        BigDecimal effectiveRate = rate.multiply(
            BigDecimal.ONE.subtract(feePercentage.divide(new BigDecimal("100")))
        );
        
        BigDecimal convertedAmount = amount.multiply(effectiveRate)
                                           .setScale(2, RoundingMode.HALF_UP);
        
        ConversionResult result = new ConversionResult(
            fromCurrency, toCurrency, amount, convertedAmount, 
            effectiveRate, feePercentage
        );
        
        transactionHistory.add(result);
        return result;
    }
    
    /**
     * Invalidate specific rate
     */
    public void invalidateRate(String fromCurrency, String toCurrency) {
        cache.invalidate(fromCurrency, toCurrency);
        System.out.println("Invalidated cache for " + fromCurrency + "/" + toCurrency);
    }
    
    /**
     * Clear all cache
     */
    public void clearCache() {
        cache.clear();
        System.out.println("Cache cleared");
    }
    
    /**
     * Get transaction history
     */
    public List<ConversionResult> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }
    
    /**
     * Get cache statistics
     */
    public void printCacheStats() {
        System.out.println("Cache size: " + cache.size() + " entries");
    }
}

// ============================================================================
// 5. DEMO / MAIN
// ============================================================================

public class CurrencyExchangeDemo {
    public static void main(String[] args) {
        System.out.println("=== CURRENCY EXCHANGE RATE SERVICE DEMO ===\n");
        
        // Initialize service with 5-minute cache TTL
        ExchangeRateAPI api = new MockExchangeRateAPI();
        CurrencyExchangeService service = new CurrencyExchangeService(api, 5);
        
        // Set transaction fee (2%)
        service.setFeePercentage(new BigDecimal("2.0"));
        
        // Test 1: EUR to GBP conversion
        System.out.println("--- Test 1: EUR to GBP ---");
        ConversionResult result1 = service.convert("EUR", "GBP", new BigDecimal("1000"));
        System.out.println("Result: " + result1 + "\n");
        
        // Test 2: Same conversion (should hit cache)
        System.out.println("--- Test 2: Same conversion (cache test) ---");
        ConversionResult result2 = service.convert("EUR", "GBP", new BigDecimal("500"));
        System.out.println("Result: " + result2 + "\n");
        
        // Test 3: Different currency pairs
        System.out.println("--- Test 3: Multiple conversions ---");
        service.convert("USD", "JPY", new BigDecimal("100"));
        service.convert("GBP", "USD", new BigDecimal("200"));
        service.convert("CAD", "AUD", new BigDecimal("300"));
        System.out.println();
        
        // Test 4: Direct rate query
        System.out.println("--- Test 4: Get exchange rate ---");
        BigDecimal eurToGbpRate = service.getExchangeRate("EUR", "GBP");
        System.out.println("EUR/GBP Rate: " + eurToGbpRate + "\n");
        
        // Test 5: Cache invalidation
        System.out.println("--- Test 5: Cache invalidation ---");
        service.invalidateRate("EUR", "GBP");
        service.getExchangeRate("EUR", "GBP"); // Should fetch again
        System.out.println();
        
        // Test 6: Transaction history
        System.out.println("--- Test 6: Transaction History ---");
        List<ConversionResult> history = service.getTransactionHistory();
        System.out.println("Total transactions: " + history.size());
        history.forEach(h -> System.out.println("  • " + h));
        System.out.println();
        
        // Test 7: Cache statistics
        System.out.println("--- Test 7: Cache Statistics ---");
        service.printCacheStats();
        
        System.out.println("\n=== DEMO COMPLETED ===");
    }
}