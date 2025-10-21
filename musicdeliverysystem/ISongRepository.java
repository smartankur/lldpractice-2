package musicdeliverysystem;

import java.util.List;
import java.util.Optional;

public interface ISongRepository {
    Song addSong(String songTitle);

    List<Song> getAllByIDs(List<Integer> songIds);

    Optional<Song> findSongById(Integer songId);
}
