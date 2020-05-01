package io.apef.base.cache;

import org.apache.commons.collections4.map.LRUMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * UnThreadSafe LURCache
 *
 * @param <K>
 * @param <V>
 */
public class LRUCache<K, V> extends LRUMap<K, V> {
    private CacheStats cacheStats = new CacheStatsUnThreadSafeImpl();

    public LRUCache(int maxSize) {
        super(maxSize);
    }

    @Override
    public V get(Object key) {
        V value = super.get(key);
        if (value == null) {
            this.cacheStats.miss();
        } else {
            this.cacheStats.hit();
        }
        return value;
    }

    public Map<K, V> getAll(Set<K> keys) {
        Map<K, V> values = new HashMap<K, V>();
        for (K key : keys) {
            V value = this.get(key);
            if (value != null) values.put(key, value);
        }
        return values;
    }

    public CacheStats stats() {
        return this.cacheStats;
    }
}
