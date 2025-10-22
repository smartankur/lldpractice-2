package musicdeliverysystem;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

public class AnalyticsManager {

    private final Map<Integer, Integer> songToTotalPlayCount;
    private final Map<Integer, Long> songToFirstPlayToday;

    // OPTIMIZATION: Incrementally maintained cache (never invalidated)
    private final Map<Integer, Set<String>> cachedSongToUniqueUsers;

    private long currentDayStart;

    public AnalyticsManager(IUserHistoryRepository userHistoryRepository) {
        this.songToTotalPlayCount = new HashMap<>();
        this.songToFirstPlayToday = new HashMap<>();
        this.cachedSongToUniqueUsers = new HashMap<>();
        this.currentDayStart = getDayStart(System.currentTimeMillis());
    }

    private long getDayStart(long timestamp) {
        return timestamp - (timestamp % Duration.ofDays(1).toMillis());
    }

    /**
     * Time Complexity: O(1) amortized
     */
    public void registerAnalytics(Song song, String userId) {
        long currentTime = System.currentTimeMillis();
        long dayStart = getDayStart(currentTime);
        Integer songId = song.getSongId();

        if (dayStart > currentDayStart) {
            resetDailyAnalytics();
            currentDayStart = dayStart;
        }

        songToTotalPlayCount.putIfAbsent(songId, 0);
        songToFirstPlayToday.putIfAbsent(songId, currentTime);
        cachedSongToUniqueUsers.putIfAbsent(songId, new HashSet<>());

        songToTotalPlayCount.put(songId, songToTotalPlayCount.get(songId) + 1);

        // OPTIMIZATION: Incremental cache update (O(1))
        cachedSongToUniqueUsers.get(songId).add(userId);
    }

    /**
     * Time Complexity: O(S log S) - always, no rebuilds needed!
     */
    public List<AnalyticsResult> getAnalytics() {
        List<AnalyticsResult> results = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : songToTotalPlayCount.entrySet()) {
            Integer songId = entry.getKey();

            results.add(AnalyticsResult.builder()
                    .songId(songId)
                    .uniqueUserCount(cachedSongToUniqueUsers.get(songId).size())
                    .totalPlayCount(entry.getValue())
                    .createdAt(songToFirstPlayToday.get(songId))
                    .build());
        }

        results.sort(Comparator
                .comparing(AnalyticsResult::getUniqueUserCount).reversed()
                .thenComparing(Comparator.comparing(AnalyticsResult::getTotalPlayCount).reversed())
                .thenComparing(AnalyticsResult::getSongId));

        return results;
    }

    public void printAnalytics() {
        List<AnalyticsResult> results = getAnalytics();
        results.forEach(result ->
                System.out.println(result.getSongId() + " " +
                        result.getUniqueUserCount() + " " +
                        result.getTotalPlayCount()));
    }

    private void resetDailyAnalytics() {
        songToTotalPlayCount.clear();
        songToFirstPlayToday.clear();
        cachedSongToUniqueUsers.clear();
    }

    public AnalyticsResult getAnalyticsForSong(Integer songId) {
        if (!songToTotalPlayCount.containsKey(songId)) {
            return null;
        }

        return AnalyticsResult.builder()
                .songId(songId)
                .uniqueUserCount(cachedSongToUniqueUsers.get(songId).size())
                .totalPlayCount(songToTotalPlayCount.get(songId))
                .createdAt(songToFirstPlayToday.get(songId))
                .build();
    }
}