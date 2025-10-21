package musicdeliverysystem;

public class AnalyticsObserver implements IObserver{

    private final AnalyticsManager analyticsManager;

    public AnalyticsObserver(AnalyticsManager analyticsManager) {
        this.analyticsManager = analyticsManager;
    }

    @Override
    public void observerSongPlay(Song song, String userId) {
        analyticsManager.registerAnalytics(song, userId);
    }
}
