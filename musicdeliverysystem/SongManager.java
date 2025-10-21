package musicdeliverysystem;

public class SongManager {

    private final ISongRepository iSongRepository;

    public SongManager(ISongRepository iSongRepository) {
        this.iSongRepository = iSongRepository;
    }

    public Integer addSong(String songTitle) {
        return iSongRepository.addSong(songTitle).getSongId();
    }
}
