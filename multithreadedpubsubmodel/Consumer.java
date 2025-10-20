package multithreadedpubsubmodel;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
@Builder
public class Consumer {
    private String id;

    @Builder.Default
    private List<SubscribedTopic> subscribedTopics = new CopyOnWriteArrayList<>();
}
