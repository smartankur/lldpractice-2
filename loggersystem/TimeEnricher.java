package loggersystem;

import java.time.LocalDateTime;

public class TimeEnricher implements LogEnricher {
    @Override
    public synchronized void enrichMessage(Message message) {
        message.setTime(LocalDateTime.now());
    }
}
