package musicdeliverysystem;

import java.util.List;

public interface IUserHistoryRepository {

    void playSong(String userId, Song song);

    List<Integer> getPlayedSongIdsInDuration(String userId, long fromHistoryTime);

    List<Integer> getNPlayedSongIdsByUser(String userId, Integer n);
}
