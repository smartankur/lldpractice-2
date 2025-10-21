package musicdeliverysystem;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

public class AnalyticsManager {

    private final Map<Integer, Integer> songToTotalPlayCount;
    private final Map<Integer, Long> songToFirstPlayToday;
    private final IUserHistoryRepository userHistoryRepository;
    private final Set<String> allUsersToday;

    private final Map<Integer, Set<String>> cachedSongToUniqueUsers;
    private boolean cacheValid = false;

    private long currentDayStart;

    public AnalyticsManager(IUserHistoryRepository userHistoryRepository) {
        this.userHistoryRepository = userHistoryRepository;
        this.songToTotalPlayCount = new HashMap<>();
        this.songToFirstPlayToday = new HashMap<>();
        this.allUsersToday = new HashSet<>();
        this.cachedSongToUniqueUsers = new HashMap<>();
        this.currentDayStart = getDayStart(System.currentTimeMillis());
    }

    private long getDayStart(long timestamp) {
        return timestamp - (timestamp % Duration.ofDays(1).toMillis());
    }

    public void registerAnalytics(Song song, String userId) {
        long currentTime = System.currentTimeMillis();
        long dayStart = getDayStart(currentTime);
        Integer songId = song.getSongId();

        // Check if it's a new day - reset analytics if needed
        if (dayStart > currentDayStart) {
            resetDailyAnalytics();
            currentDayStart = dayStart;
        }

        // Track this user
        allUsersToday.add(userId);

        // Initialize maps for this song if first play today
        songToTotalPlayCount.putIfAbsent(songId, 0);
        songToFirstPlayToday.putIfAbsent(songId, currentTime);

        // Increment total play count
        songToTotalPlayCount.put(songId, songToTotalPlayCount.get(songId) + 1);

        // Invalidate cache since data changed
        cacheValid = false;
    }

    /**
     * Build cache of unique users per song by querying UserHistoryRepository
     * This inverts the query: iterate users once, collect all their songs
     */
    private void buildUniqueUsersCache(long dayStart) {
        cachedSongToUniqueUsers.clear();

        // For each user, get all songs they played today
        for (String userId : allUsersToday) {
            List<Integer> songsPlayedToday = userHistoryRepository
                    .getPlayedSongIdsInDuration(userId, dayStart);

            // For each song this user played, add user to that song's set
            for (Integer songId : songsPlayedToday) {
                cachedSongToUniqueUsers.putIfAbsent(songId, new HashSet<>());
                cachedSongToUniqueUsers.get(songId).add(userId);
            }
        }

        cacheValid = true;
    }

    /**
     * Get unique user count for a song (uses cache)
     */
    private int getUniqueUserCountForSong(Integer songId, long dayStart) {
        // Build cache if invalid
        if (!cacheValid) {
            buildUniqueUsersCache(dayStart);
        }

        Set<String> uniqueUsers = cachedSongToUniqueUsers.get(songId);
        return uniqueUsers != null ? uniqueUsers.size() : 0;
    }

    public List<AnalyticsResult> getAnalytics() {
        List<AnalyticsResult> results = new ArrayList<>();
        long dayStart = getDayStart(System.currentTimeMillis());

        // Ensure cache is built before iterating
        if (!cacheValid) {
            buildUniqueUsersCache(dayStart);
        }

        // Build results
        for (Map.Entry<Integer, Integer> entry : songToTotalPlayCount.entrySet()) {
            Integer songId = entry.getKey();

            results.add(AnalyticsResult.builder()
                    .songId(songId)
                    .uniqueUserCount(getUniqueUserCountForSong(songId, dayStart))
                    .totalPlayCount(entry.getValue())
                    .createdAt(songToFirstPlayToday.get(songId))
                    .build());
        }

        // Sort: uniqueUserCount DESC -> totalPlayCount DESC -> songId ASC
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
        allUsersToday.clear();
        cachedSongToUniqueUsers.clear();
        cacheValid = false;
    }

    public AnalyticsResult getAnalyticsForSong(Integer songId) {
        if (!songToTotalPlayCount.containsKey(songId)) {
            return null;
        }

        long dayStart = getDayStart(System.currentTimeMillis());

        return AnalyticsResult.builder()
                .songId(songId)
                .uniqueUserCount(getUniqueUserCountForSong(songId, dayStart))
                .totalPlayCount(songToTotalPlayCount.get(songId))
                .createdAt(songToFirstPlayToday.get(songId))
                .build();
    }
}
