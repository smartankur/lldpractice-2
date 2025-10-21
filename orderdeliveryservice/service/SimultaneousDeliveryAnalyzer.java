package orderdeliveryservice.service;

import orderdeliveryservice.model.Delivery;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzes simultaneous (overlapping) deliveries across drivers.
 *
 * Use cases:
 * - Fleet management and capacity planning
 * - Fraud detection (drivers with impossible overlaps)
 * - Peak hour identification
 * - Resource allocation optimization
 */
public class SimultaneousDeliveryAnalyzer {

    /**
     * Represents a group of overlapping deliveries.
     */
    @Getter
    @AllArgsConstructor
    public static class OverlapGroup {
        private List<Delivery> deliveries;
        private long overlapStart;
        private long overlapEnd;

        public int getDeliveryCount() {
            return deliveries.size();
        }

        public long getOverlapDurationSeconds() {
            return overlapEnd - overlapStart;
        }

        @Override
        public String toString() {
            return String.format("OverlapGroup{count=%d, duration=%ds, start=%d, end=%d}",
                deliveries.size(), getOverlapDurationSeconds(), overlapStart, overlapEnd);
        }
    }

    /**
     * Represents a point in time with concurrency information.
     */
    @Getter
    @AllArgsConstructor
    public static class ConcurrencyPoint {
        private long timestamp;
        private int concurrentDeliveries;
        private List<String> driverIds;

        @Override
        public String toString() {
            return String.format("ConcurrencyPoint{time=%d, concurrent=%d, drivers=%s}",
                timestamp, concurrentDeliveries, driverIds);
        }
    }

    /**
     * Finds all pairs of deliveries that overlap in time.
     *
     * Two deliveries overlap if their time windows intersect.
     *
     * Time Complexity: O(n^2) - naive approach
     * Space Complexity: O(k) where k = number of overlapping pairs
     *
     * @param deliveries list of all deliveries
     * @return list of overlapping delivery pairs
     */
    public List<OverlapGroup> findOverlappingDeliveries(List<Delivery> deliveries) {
        List<OverlapGroup> overlaps = new ArrayList<>();

        if (deliveries == null || deliveries.size() < 2) {
            return overlaps;
        }

        // Check each pair
        for (int i = 0; i < deliveries.size(); i++) {
            for (int j = i + 1; j < deliveries.size(); j++) {
                Delivery d1 = deliveries.get(i);
                Delivery d2 = deliveries.get(j);

                if (doIntervalsOverlap(d1.getStartTime(), d1.getEndTime(),
                                      d2.getStartTime(), d2.getEndTime())) {

                    // Calculate overlap period
                    long overlapStart = Math.max(d1.getStartTime(), d2.getStartTime());
                    long overlapEnd = Math.min(d1.getEndTime(), d2.getEndTime());

                    overlaps.add(new OverlapGroup(
                        Arrays.asList(d1, d2),
                        overlapStart,
                        overlapEnd
                    ));
                }
            }
        }

        return overlaps;
    }

    /**
     * Checks if two time intervals overlap.
     *
     * Intervals [a1, a2] and [b1, b2] overlap if: a1 < b2 AND b1 < a2
     *
     * @return true if intervals overlap
     */
    private boolean doIntervalsOverlap(long a1, long a2, long b1, long b2) {
        return a1 < b2 && b1 < a2;
    }

    /**
     * Finds the maximum number of concurrent deliveries at any point in time.
     *
     * Algorithm: Sweep Line
     * 1. Create events for each delivery start (+1) and end (-1)
     * 2. Sort events by timestamp
     * 3. Sweep through events, tracking running count
     * 4. Record maximum
     *
     * Time Complexity: O(n log n) - dominated by sorting
     * Space Complexity: O(n)
     *
     * @param deliveries list of all deliveries
     * @return maximum concurrent deliveries count
     */
    public int findMaxConcurrentDeliveries(List<Delivery> deliveries) {
        if (deliveries == null || deliveries.isEmpty()) {
            return 0;
        }

        // Create events
        List<Event> events = new ArrayList<>();
        for (Delivery d : deliveries) {
            events.add(new Event(d.getStartTime(), 1));  // Delivery starts
            events.add(new Event(d.getEndTime(), -1));   // Delivery ends
        }

        // Sort by timestamp; if equal, process ends (-1) before starts (+1)
        events.sort((e1, e2) -> {
            if (e1.timestamp != e2.timestamp) {
                return Long.compare(e1.timestamp, e2.timestamp);
            }
            return Integer.compare(e1.delta, e2.delta); // -1 comes before +1
        });

        // Sweep through events
        int currentConcurrent = 0;
        int maxConcurrent = 0;

        for (Event event : events) {
            currentConcurrent += event.delta;
            maxConcurrent = Math.max(maxConcurrent, currentConcurrent);
        }

        return maxConcurrent;
    }

    /**
     * Finds the exact moment(s) when maximum concurrency occurs.
     *
     * Useful for identifying peak hours and resource planning.
     *
     * @param deliveries list of all deliveries
     * @return list of timestamps with peak concurrency
     */
    public List<ConcurrencyPoint> findPeakConcurrencyPoints(List<Delivery> deliveries) {
        if (deliveries == null || deliveries.isEmpty()) {
            return Collections.emptyList();
        }

        List<Event> events = new ArrayList<>();
        for (Delivery d : deliveries) {
            events.add(new Event(d.getStartTime(), 1, d.getDriverId()));
            events.add(new Event(d.getEndTime(), -1, d.getDriverId()));
        }

        events.sort((e1, e2) -> {
            if (e1.timestamp != e2.timestamp) {
                return Long.compare(e1.timestamp, e2.timestamp);
            }
            return Integer.compare(e1.delta, e2.delta);
        });

        int currentConcurrent = 0;
        int maxConcurrent = 0;
        Set<String> activeDrivers = new HashSet<>();
        List<ConcurrencyPoint> peakPoints = new ArrayList<>();

        for (Event event : events) {
            if (event.delta == 1) {
                activeDrivers.add(event.driverId);
            } else {
                activeDrivers.remove(event.driverId);
            }

            currentConcurrent += event.delta;

            if (currentConcurrent > maxConcurrent) {
                maxConcurrent = currentConcurrent;
                peakPoints.clear();
                peakPoints.add(new ConcurrencyPoint(
                    event.timestamp,
                    currentConcurrent,
                    new ArrayList<>(activeDrivers)
                ));
            } else if (currentConcurrent == maxConcurrent && currentConcurrent > 0) {
                peakPoints.add(new ConcurrencyPoint(
                    event.timestamp,
                    currentConcurrent,
                    new ArrayList<>(activeDrivers)
                ));
            }
        }

        return peakPoints;
    }

    /**
     * Finds all deliveries by a specific driver that overlap.
     *
     * Useful for fraud detection - a single driver shouldn't have
     * multiple simultaneous deliveries (physically impossible).
     *
     * @param deliveries all deliveries in the system
     * @param driverId the driver to check
     * @return list of overlapping delivery groups for this driver
     */
    public List<OverlapGroup> findDriverSelfOverlaps(List<Delivery> deliveries, String driverId) {
        if (deliveries == null || driverId == null) {
            return Collections.emptyList();
        }

        List<Delivery> driverDeliveries = deliveries.stream()
            .filter(d -> d.getDriverId().equals(driverId))
            .sorted(Comparator.comparing(Delivery::getStartTime))
            .collect(Collectors.toList());

        return findOverlappingDeliveries(driverDeliveries);
    }

    /**
     * Groups deliveries by time period and counts them.
     *
     * Useful for capacity planning and identifying busy periods.
     *
     * @param deliveries all deliveries
     * @param bucketSizeSeconds size of each time bucket (e.g., 3600 for hourly)
     * @return map of bucket start time to delivery count
     */
    public Map<Long, Integer> getDeliveryCountsByTimeBucket(List<Delivery> deliveries, long bucketSizeSeconds) {
        Map<Long, Integer> buckets = new TreeMap<>();

        if (deliveries == null || bucketSizeSeconds <= 0) {
            return buckets;
        }

        for (Delivery d : deliveries) {
            long bucketStart = (d.getStartTime() / bucketSizeSeconds) * bucketSizeSeconds;
            buckets.merge(bucketStart, 1, Integer::sum);
        }

        return buckets;
    }

    /**
     * Analyzes if any driver has suspicious overlapping deliveries.
     *
     * @param deliveries all deliveries
     * @return map of driver ID to number of self-overlaps
     */
    public Map<String, Integer> detectSuspiciousDrivers(List<Delivery> deliveries) {
        if (deliveries == null) {
            return Collections.emptyMap();
        }

        Map<String, Integer> suspiciousDrivers = new HashMap<>();

        Set<String> driverIds = deliveries.stream()
            .map(Delivery::getDriverId)
            .collect(Collectors.toSet());

        for (String driverId : driverIds) {
            List<OverlapGroup> overlaps = findDriverSelfOverlaps(deliveries, driverId);
            if (!overlaps.isEmpty()) {
                suspiciousDrivers.put(driverId, overlaps.size());
            }
        }

        return suspiciousDrivers;
    }

    // Helper class for sweep line algorithm
    @AllArgsConstructor
    private static class Event {
        long timestamp;
        int delta;  // +1 for start, -1 for end
        String driverId;

        Event(long timestamp, int delta) {
            this(timestamp, delta, null);
        }
    }
}