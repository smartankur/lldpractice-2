package loggersystem;

import lombok.Getter;

@Getter
public enum Level {
    INFO(1),
    DEBUG(0),
    ERROR(3),
    FATAL(4),
    WARN(2);

    private Integer priority;

    Level(Integer priority) {
       this.priority = priority;
    }

    public boolean isHigherOrEqualPriority(Level other) {
        return this.priority >= other.priority;
    }
}
