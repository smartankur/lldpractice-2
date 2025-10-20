package multithreadedpubsubmodel;

import lombok.Data;
/*
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class KafkaRepository {

    private final ConcurrentHashMap<String, List<Pair<String, Integer>>> consumerToTopicToOffset = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<String>> topicsToConsumers = new ConcurrentHashMap<>();
    private final Map<String, Topic> topicIdToTopics = new HashMap<>();

    public Optional<Integer> getTopicOffSetByConsumer(String consumerId, String topicId) {
        List<Pair<String, Integer>> topicsToOffsets = consumerToTopicToOffset.getOrDefault(consumerId, Collections.emptyList());
        return topicsToOffsets.stream()
                .filter(topicsToOffset -> topicsToOffset.getTopicId().equals(topicId))
                .map(Pair::getOffsetId)
                .findFirst();
    }


    public void saveOffsetForAConsumer(String consumerId, SubscribedTopic subscribedTopic) {
        List<Pair<String, Integer>> topicsToOffsets = consumerToTopicToOffset.getOrDefault(consumerId, Collections.emptyList());
        topicsToOffsets.stream()
                .filter(topicsToOffset -> topicsToOffset.getTopicId().equals(subscribedTopic.getTopicId()))
                .forEach(topicsToOffset -> topicsToOffset.setOffsetId(subscribedTopic.getOffsetId()));
    }

    public Topic getTopicById(String topicId) {
        return topicIdToTopics.getOrDefault(topicId, null);
    }

    public void addTopic(Topic topic) {
        topicIdToTopics.putIfAbsent(topic.getId(), topic);
    }

    public void saveMessageToTopic(String topicId, Message message) {
        var topic = getTopicById(topicId);
        if (topic != null) {
            topic.getMessages().putIfAbsent(message.getId(), message);
        }
    }

    public void addConsumersForTopic(String consumerId, String topicId) {
        topicsToConsumers.computeIfAbsent(consumerId, p -> new ArrayList<>()).add(topicId);
        consumerToTopicToOffset.computeIfAbsent(consumerId, p -> new ArrayList<>()).add(new Pair<>(topicId, 0));
    }

    public List<String> getConsumersForTopic(String topicId) {
        return topicsToConsumers.getOrDefault(topicId, Collections.emptyList());
    }
}
*/