package musicdeliverysystem;

import java.util.List;

public class UserPlaylistManager {
    private final IUserHistoryRepository userHistoryRepository;
    private final SongRepository songRepository;
    private final List<IObserver> iObservers;

    public UserPlaylistManager(IUserHistoryRepository userHistoryRepository, SongRepository songRepository,
                               List<IObserver> iObservers) {
        this.userHistoryRepository = userHistoryRepository;
        this.songRepository = songRepository;
        this.iObservers = iObservers;
    }

    public boolean playSong(Integer songId, String userId) {
        var song = songRepository.findSongById(songId).orElseThrow(IllegalArgumentException::new);
        userHistoryRepository.playSong(userId, song);
        iObservers.forEach(iObserver -> iObserver.observerSongPlay(song, userId));
        return true;
    }

    public List<Song> getLastNSongsPlayedByUser(String userId, Integer n) {
        var lastNSongs = userHistoryRepository.getNPlayedSongIdsByUser(userId, n);
        return songRepository.getAllByIDs(lastNSongs);
    }
}
