package musicdeliverysystem;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class UserHistoryRepository implements IUserHistoryRepository {

    private final Map<String, LinkedHashMap<Integer, Long>> usersToPlayedSongs;

    public UserHistoryRepository() {
        usersToPlayedSongs = new HashMap<>();
    }

    @Override
    public void playSong(String userId, Song song) {
        usersToPlayedSongs.putIfAbsent(userId, new LinkedHashMap<>());
        LinkedHashMap<Integer, Long> history = usersToPlayedSongs.get(userId);

        Integer songId = song.getSongId();
        long currentTime = System.currentTimeMillis();

        // Remove old entry if exists (updates position to most recent)
        history.remove(songId);

        // Add at end (most recent)
        history.put(songId, currentTime);
    }

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
        Collections.reverse(result); // Most recent first

        return result;
    }

    @Override
    public List<Integer> getPlayedSongIdsInDuration(String userId, long fromHistoryTime) {
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
