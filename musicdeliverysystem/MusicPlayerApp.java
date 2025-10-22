package musicdeliverysystem;

import java.util.List;

public class MusicPlayerApp {

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════╗");
        System.out.println("║    🎵 OPTIMIZED MUSIC PLAYER SYSTEM 🎵        ║");
        System.out.println("╚════════════════════════════════════════════════╝\n");

        // Initialize system
        SongRepository songRepo = new SongRepository();
        UserHistoryRepository userHistoryRepo = new UserHistoryRepository();
        AnalyticsManager analyticsManager = new AnalyticsManager(userHistoryRepo);
        AnalyticsObserver analyticsObserver = new AnalyticsObserver(analyticsManager);

        UserPlaylistManager playlistManager = new UserPlaylistManager(
            userHistoryRepo, songRepo, List.of(analyticsObserver)
        );
        SongManager songManager = new SongManager(songRepo);

        // Demo
        System.out.println("📝 Adding songs...");
        int song1 = songManager.addSong("Blinding Lights");
        int song2 = songManager.addSong("Shape of You");
        int song3 = songManager.addSong("Levitating");
        System.out.println("✓ Added 3 songs\n");

        System.out.println("▶️  Playing songs...");
        playlistManager.playSong(song1, "alice");
        playlistManager.playSong(song1, "bob");
        playlistManager.playSong(song1, "alice"); // replay
        playlistManager.playSong(song2, "alice");
        playlistManager.playSong(song2, "charlie");
        playlistManager.playSong(song3, "alice");
        System.out.println("✓ Registered 6 plays\n");

        System.out.println("📊 Analytics (SongID UniqueUsers TotalPlays):");
        System.out.println("-----------------------------------------------");
        analyticsManager.printAnalytics();
        System.out.println();

        System.out.println("🎧 Alice's last 3 songs (most recent first):");
        List<Song> aliceSongs = playlistManager.getLastNSongsPlayedByUser("alice", 3);
        aliceSongs.forEach(song -> 
            System.out.println("  - " + song.getSongId() + ": " + song.getSongTitle())
        );

        System.out.println("\n╔════════════════════════════════════════════════╗");
        System.out.println("║              ✅ Demo Complete!                ║");
        System.out.println("╚════════════════════════════════════════════════╝");
    }
}