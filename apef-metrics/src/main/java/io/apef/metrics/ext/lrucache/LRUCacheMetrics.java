package io.apef.metrics.ext.lrucache;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricSet;
import io.apef.base.cache.LRUCache;
import io.apef.metrics.ext.AbstractExtMetrics;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.codahale.metrics.MetricRegistry.name;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LRUCacheMetrics extends AbstractExtMetrics {

    public static MetricSet metricsFor(String cacheName, final LRUCache cache) {

        LRUCacheMetrics metrics = new LRUCacheMetrics();

        metrics.put(name(cacheName, "hitRate"), (Gauge<Double>) () -> cache.stats().hitRate());

        metrics.put(name(cacheName, "hitCount"), (Gauge<Long>) () -> cache.stats().hitCount());

        metrics.put(name(cacheName, "missCount"), (Gauge<Long>) () -> cache.stats().missCount());

        return metrics;
    }
}
