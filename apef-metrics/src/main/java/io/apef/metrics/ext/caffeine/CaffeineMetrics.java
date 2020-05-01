package io.apef.metrics.ext.caffeine;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricSet;
import io.apef.metrics.ext.AbstractExtMetrics;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.codahale.metrics.MetricRegistry.name;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CaffeineMetrics extends AbstractExtMetrics {

    public static MetricSet metricsFor(String cacheName, final Cache cache) {

        CaffeineMetrics metrics = new CaffeineMetrics();

        metrics.put(name(cacheName, "hitRate"), (Gauge<Double>) () -> cache.stats().hitRate());

        metrics.put(name(cacheName, "hitCount"), (Gauge<Long>) () -> cache.stats().hitCount());

        metrics.put(name(cacheName, "missCount"), (Gauge<Long>) () -> cache.stats().missCount());

        return metrics;
    }

}
