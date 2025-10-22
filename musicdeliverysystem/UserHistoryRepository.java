package musicdeliverysystem;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class UserHistoryRepository implements IUserHistoryRepository {

    private final Map<String, LinkedHashMap<Integer, Long>> usersToPlayedSongs;

    // OPTIMIZATION: Track today's songs separately for O(S_today) access
    private final Map<String, Set<Integer>> userToTodaySongs;
    private long currentDayStart;

    public UserHistoryRepository() {
        usersToPlayedSongs = new HashMap<>();
        userToTodaySongs = new HashMap<>();
        currentDayStart = getDayStart(System.currentTimeMillis());
    }

    private long getDayStart(long timestamp) {
        return timestamp - (timestamp % Duration.ofDays(1).toMillis());
    }

    /**
     * Time Complexity: O(1) average
     */
    @Override
    public void playSong(String userId, Song song) {
        long currentTime = System.currentTimeMillis();
        long dayStart = getDayStart(currentTime);
        Integer songId = song.getSongId();

        if (dayStart > currentDayStart) {
            userToTodaySongs.clear();
            currentDayStart = dayStart;
        }

        // Update full history (for lastN songs)
        usersToPlayedSongs.putIfAbsent(userId, new LinkedHashMap<>());
        LinkedHashMap<Integer, Long> history = usersToPlayedSongs.get(userId);
        history.remove(songId);
        history.put(songId, currentTime);

        // OPTIMIZATION: Update today's set (O(1))
        userToTodaySongs.putIfAbsent(userId, new HashSet<>());
        userToTodaySongs.get(userId).add(songId);
    }

    /**
     * Time Complexity: O(N)
     */
    @Override
    public List<Integer> getNPlayedSongIdsByUser(String userId, Integer n) {
        if (!usersToPlayedSongs.containsKey(userId)) {
            return Collections.emptyList();
        }

        LinkedHashMap<Integer, Long> history = usersToPlayedSongs.get(userId);
        List<Integer> allSongs = new ArrayList<>(history.keySet());

        int size = allSongs.size();
        int startIdx = Math.max(0, size - n);

        List<Integer> result = allSongs.subList(startIdx, size);
        Collections.reverse(result);

        return result;
    }

    /**
     * Time Complexity: O(S_today) for today, O(S_ever) for historical
     */
    @Override
    public List<Integer> getPlayedSongIdsInDuration(String userId, long fromHistoryTime) {
        long queryDayStart = getDayStart(fromHistoryTime);

        // OPTIMIZATION: Fast path for today's songs (O(S_today))
        if (queryDayStart == currentDayStart) {
            Set<Integer> todaySongs = userToTodaySongs.get(userId);
            return todaySongs != null ? new ArrayList<>(todaySongs) : Collections.emptyList();
        }

        // Slow path for historical queries
        if (!usersToPlayedSongs.containsKey(userId)) {
            return Collections.emptyList();
        }

        LinkedHashMap<Integer, Long> songHistory = usersToPlayedSongs.get(userId);

        return songHistory.entrySet().stream()
                .filter(entry -> entry.getValue() >= fromHistoryTime)
                .map(Map.Entry::getKey)
                .toList();
    }
}