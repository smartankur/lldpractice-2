package multithreadedpubsubmodel;

import lombok.Builder;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
@Builder
public class SubscribedTopic {
    private String topicId;

    @Builder.Default
    private AtomicInteger offsetId = new AtomicInteger(0);
}