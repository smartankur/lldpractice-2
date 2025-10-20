package loggersystem;

public interface Sink {

    public abstract SinkType getSinkType();

    void logMessage(SinkDetails sinkDetails);

    void setPriorityLevelForLevel(Level level);

    boolean logMessageAboveLevel(Level level);
}
