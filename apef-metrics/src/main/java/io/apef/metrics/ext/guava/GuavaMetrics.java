package io.apef.metrics.ext.guava;

import static com.codahale.metrics.MetricRegistry.name;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricSet;
import io.apef.metrics.ext.AbstractExtMetrics;
import com.google.common.cache.Cache;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GuavaMetrics extends AbstractExtMetrics {

    public static MetricSet metricsFor(String cacheName, final Cache cache) {

        GuavaMetrics metrics = new GuavaMetrics();

        metrics.put(name(cacheName, "hitRate"), (Gauge<Double>) () -> cache.stats().hitRate());

        metrics.put(name(cacheName, "hitCount"), (Gauge<Long>) () -> cache.stats().hitCount());

        metrics.put(name(cacheName, "missCount"), (Gauge<Long>) () -> cache.stats().missCount());

        return metrics;
    }
}
