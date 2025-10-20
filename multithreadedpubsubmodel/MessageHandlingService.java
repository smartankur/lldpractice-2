package multithreadedpubsubmodel;

/*
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class MessageHandlingService {
    ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    private final KafkaRepository kafkaRepository = new KafkaRepository();

    public boolean publishMessage(Producer producer, Message message,
                                  String topicId) {
        reentrantReadWriteLock.writeLock().lock();
        try {
            var isProducerSubscribedToGivenTopic = producer.getSubscribedTopics().stream()
                    .map(SubscribedTopic::getTopicId).filter(tId -> tId.equals(topicId))
                    .findAny();
            if (isProducerSubscribedToGivenTopic.isEmpty()) {
                throw new IllegalArgumentException("Producer is not subscribed to this topic");
            }
            kafkaRepository.saveMessageToTopic(topicId, message);
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
        return true;
    }
    // Class-level map for topic-consumer specific locks
    private final ConcurrentHashMap<String, ReentrantLock> topicConsumerLocks = new ConcurrentHashMap<>();

    public List<Message> consumeMessages(Consumer consumer) {
        List<SubscribedTopic> subscribedTopics = consumer.getSubscribedTopics();
        List<CompletableFuture<Message>> topicFutures = subscribedTopics.stream()
                .map(subscribedTopic -> CompletableFuture.supplyAsync(() -> {
                    String lockKey = consumer.getId() + ":" + subscribedTopic.getTopicId();
                    ReentrantLock lock = topicConsumerLocks.computeIfAbsent(lockKey, k -> new ReentrantLock());

                    lock.lock();
                    try {
                        return consumeFromTopic(consumer, subscribedTopic);
                    } finally {
                        lock.unlock();
                    }
                }))
                .toList();

        return CompletableFuture.allOf(topicFutures.toArray(new CompletableFuture[0]))
                .thenApply(v ->
                        topicFutures.stream()
                                .map(CompletableFuture::join)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList())
                ).join();
    }

    private Message consumeFromTopic(Consumer consumer, SubscribedTopic subscribedTopic) {
        var topicDetails = kafkaRepository.getTopicById(subscribedTopic.getTopicId());
        var consumerOffSet = kafkaRepository.getTopicOffSetByConsumer(consumer.getId(), topicDetails.getId());
        if(consumerOffSet.isEmpty()) {
            consumerOffSet = Optional.of(0);
        }

        Message message = topicDetails.getMessages().getOrDefault(consumerOffSet.get(), null);

        if (message != null) {
            // Atomically update both in-memory and database offset
            int newOffset = consumerOffSet.get() + 1;
            subscribedTopic.getOffsetId().set(newOffset);
            kafkaRepository.saveOffsetForAConsumer(consumer.getId(), subscribedTopic);
        }

        return message;
    }

}
*/