package io.apef.connector.sender;

import io.apef.base.cache.LRUCache;

public class SenderChannelCache<K, R> extends LRUCache<K, R> {
    private SenderChannelCache(int maxSize) {
        super(maxSize);
    }

    public static <K, R> SenderChannelCache<K, R> build(int maxSize) {
        if (maxSize < 1) return null;
        return new SenderChannelCache<>(maxSize);
    }
}
