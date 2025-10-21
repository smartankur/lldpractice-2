package musicdeliverysystem;

import java.util.*;

public class SongRepository implements ISongRepository {

    private final Map<Integer, Song> songs;
    private Integer nextSongId;

    public SongRepository() {
        songs = new HashMap<>();
        nextSongId = 0;
    }

    @Override
    public Song addSong(String songTitle) {
        Song song = Song.builder()
                .songId(nextSongId)
                .songTitle(songTitle)
                .build();
        songs.put(nextSongId, song);
        nextSongId++;
        return song;
    }

    @Override
    public List<Song> getAllByIDs(List<Integer> songIds) {
        return songIds.stream()
                .map(songId -> songs.getOrDefault(songId, null))
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Optional<Song> findSongById(Integer songId) {
        return Optional.ofNullable(songs.get(songId));
    }

}
