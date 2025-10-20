package concurrentcachesystem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

class ConcurrentCache<K, V> implements Cache<K, V> {

    private final int maxCapacity;
    private final ConcurrentHashMap<K, CacheEntry<V>> cache;
    private final ScheduledExecutorService cleanupExecutor;
    private Node head;
    private Node tail;
    private final ReentrantReadWriteLock reentrantLock;

    private final AtomicLong hits = new AtomicLong(0);
    private final AtomicLong misses = new AtomicLong(0);
    private final AtomicLong evictions = new AtomicLong(0);
    private final AtomicLong expirations = new AtomicLong(0);


    public ConcurrentCache(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        this.cache = new ConcurrentHashMap<>(maxCapacity);
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        head = new Node(-1);
        tail = new Node(-1);
        head.next = tail;
        tail.prev = head;

        // TODO: Initialize your LRU structure

        // TODO: Start background cleanup thread
        startCleanupTask();
        reentrantLock = new ReentrantReadWriteLock();
    }

    private void startCleanupTask() {
        cleanupExecutor.scheduleAtFixedRate(() -> {
            var now = System.currentTimeMillis();
            var keysToBeEvicted = cache.entrySet()
                    .stream().filter(kCacheEntryEntry -> kCacheEntryEntry.getValue().isExpired(now))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());
            keysToBeEvicted.forEach(k -> {
                reentrantLock.writeLock().lock();
                try {
                    deleteNode(cache.get(k).location);
                    cache.remove(k);
                    expirations.incrementAndGet();
                } finally {
                    reentrantLock.writeLock().unlock();
                }
            });

        }, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void put(K key, V value, long ttlMillis) {
        reentrantLock.writeLock().lock();
        try {
            if (cache.size() == maxCapacity && !cache.containsKey(key)) {
                Node node = tail.prev;
                cache.remove(node.key);
                deleteNode(node);
                evictions.incrementAndGet();
            }
            var existingValue = cache.getOrDefault(key, null);
            var now = System.currentTimeMillis();
            var newNode = new Node(key);
            cache.put(key, CacheEntry.of(value, ttlMillis, now, newNode));
            if (existingValue != null) {
                deleteNode(existingValue.location);
            }
            insertAfterHead(newNode);
        } finally {
            reentrantLock.writeLock().unlock();
        }
    }

    @Override
    public V get(K key) {
        CacheEntry<V> e = cache.get(key);
        if (e == null) { misses.incrementAndGet(); return null; }

        long now = System.currentTimeMillis();
        if (e.isExpired(now)) {
            if (cache.remove(key, e)) {
                reentrantLock.writeLock().lock();
                try { deleteNode(e.location); } finally { reentrantLock.writeLock().unlock(); }
                expirations.incrementAndGet();
            }
            misses.incrementAndGet();
            return null;
        }

        // Move to head under lock
        reentrantLock.writeLock().lock();
        try { insertAfterHead(e.location); } finally { reentrantLock.writeLock().unlock(); }
        hits.incrementAndGet();
        return e.value;
    }

    @Override
    public CacheStats getStats() {
        return CacheStats.builder()
                .currentSize(cache.size())
                .expirations(expirations.get())
                .hits(hits.get())
                .misses(misses.get())
                .evictions(evictions.get()).build();
    }

    @Override
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            cleanupExecutor.awaitTermination(10, TimeUnit.SECONDS);
            cleanupExecutor.shutdownNow();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }
    }

    private void deleteNode(Node node) {
        Node prevNode = node.prev;
        Node nextNode = node.next;
        prevNode.next = nextNode;
        nextNode.prev = prevNode;
    }

    private void insertAfterHead(Node node) {
        Node headNext = head.next;
        head.next = node;
        node.next = headNext;
        node.prev = head;
        headNext.prev = node;
    }
}