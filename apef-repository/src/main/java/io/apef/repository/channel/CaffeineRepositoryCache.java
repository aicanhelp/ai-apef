package io.apef.repository.channel;

import io.apef.base.cache.CacheStats;
import io.apef.base.utils.KeyMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.apef.metrics.ApefMetricsFactory;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.Set;

public class CaffeineRepositoryCache<K, V> implements RepositoryCache<K, V> {
    private String name;
    private Cache<K, V> cache;
    private KeyMapper<K, V> keyMapper;

    public CaffeineRepositoryCache(String name,
                                   KeyMapper<K, V> keyMapper,
                                   int maxCacheSize, boolean reportMetrics) {
        this.name = name;
        this.keyMapper = keyMapper;
        this.cache.stats();
        if (maxCacheSize > 0) {
            this.cache = Caffeine.newBuilder().maximumSize(maxCacheSize).build();
        }
        if (reportMetrics)
            ApefMetricsFactory.defaultMetrics().registerCaffeine(name, this.cache);
    }

    @Override
    public V get(K key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void save(V value) {
        cache.put(this.keyMapper.keyOf(value), value);
    }

    @Override
    public boolean exists(K key) {
        return cache.getIfPresent(key) != null;
    }

    @Override
    public Map<K, V> getAll(Set<K> keys) {
        return cache.getAllPresent(keys);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> values) {
        cache.putAll(values);
    }

    @Override
    public CacheStats stats() {
        return new CacheStatsWrapper(this.cache.stats());
    }

    @Getter
    @Accessors(fluent = true)
    private static class CacheStatsWrapper
            implements CacheStats {
        private double hitRate;
        private long hitCount;
        private long missCount;

        public CacheStatsWrapper(com.github.benmanes.caffeine.cache.stats.CacheStats cacheStats) {
            this.hitRate = cacheStats.hitRate();
            this.hitCount = cacheStats.hitCount();
            this.missCount = cacheStats.missCount();
        }

        @Override
        public void hit() {

        }

        @Override
        public void miss() {

        }

        @Override
        public void hit(int count) {

        }

        @Override
        public void miss(int count) {

        }

        @Override
        public void reset() {

        }
    }
}
