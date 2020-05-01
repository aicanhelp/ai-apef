package io.apef.repository.channel;

import io.apef.base.cache.LRUCache;
import io.apef.base.utils.KeyMapper;
import io.apef.metrics.ApefMetricsFactory;
import io.apef.repository.RepositoryConfig;

import java.util.Map;

public class LRURepositoryCache<K, V> extends LRUCache<K, V> implements RepositoryCache<K, V> {
    private String name;
    private KeyMapper<K, V> keyMapper;

    public LRURepositoryCache(RepositoryConfig repositoryConfig) {
        this(repositoryConfig.getName() + "Cache", repositoryConfig.getKeyMapper(),
                repositoryConfig.getMaxCachedSize(), repositoryConfig.isEnableCacheMetrics());
    }

    public LRURepositoryCache(String name,
                              KeyMapper<K, V> keyMapper,
                              int maxCacheSize,
                              boolean reportMetrics) {
        super(maxCacheSize);
        this.name = name;
        this.keyMapper = keyMapper;
        if (reportMetrics)
            ApefMetricsFactory.defaultMetrics()
                    .registerLRUCache(name, this);
    }

    @Override
    public void save(V value) {
        put(this.keyMapper.keyOf(value), value);
    }

    @Override
    public boolean exists(K key) {
        return containsKey(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        super.putAll(map);
    }
}