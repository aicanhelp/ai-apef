package io.apef.repository.channel;

import io.apef.base.cache.CacheStats;

import java.util.Map;
import java.util.Set;

public interface RepositoryCache<K, V> {
    V get(K key);

    void save(V value);

    boolean exists(K key);

    Map<K, V> getAll(Set<K> keys);

    void putAll(Map<? extends K, ? extends V> values);

    CacheStats stats();
}
