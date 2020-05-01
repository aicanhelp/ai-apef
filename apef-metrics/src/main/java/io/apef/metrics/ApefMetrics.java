package io.apef.metrics;

import io.apef.base.cache.LRUCache;
import io.apef.base.utils.ObjectFormatter;
import io.apef.metrics.config.MetricsConfig;
import io.apef.metrics.config.ApefMetricsConfig;
import io.apef.metrics.ext.caffeine.CaffeineMetrics;
import io.apef.metrics.ext.guava.GuavaMetrics;
import io.apef.metrics.ext.lrucache.LRUCacheMetrics;
import io.apef.metrics.item.MetricItem;
import io.apef.metrics.item.MetricItemType;
import io.apef.metrics.reporter.MetricsReporterFactory;
import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApefMetrics {
    private final static String CACHE_METRIC = "Built_Cache";
    @Getter
    @Accessors(fluent = true)
    private ApefMetricsConfig config;
    @Getter
    @Accessors(fluent = true)
    private MetricsReporterFactory reporterFactory;
    private ApefMetric cacheMetric;

    private Map<String, ApefMetric> vexMetricMap = new ConcurrentHashMap<>();

    public ApefMetrics(ApefMetricsConfig config) {
        this.config = config;
        this.reporterFactory = new MetricsReporterFactory(config.getReporters());
        this.config.getMetricsFactory()
                .forEach((s, metricsConfig) -> {
                    vexMetricMap.put(s, new ApefMetric(metricsConfig, reporterFactory));
                });
        this.initCacheMetric();
    }

    private void initCacheMetric() {
        MetricsConfig config = this.config.getMetricsFactory().get(CACHE_METRIC);
        if (config == null) {
            config = this.createVexMetricConfig(CACHE_METRIC);
        }
        this.cacheMetric = new ApefMetric(config, this.reporterFactory);
    }

    private MetricsConfig createVexMetricConfig(String name) {
        MetricsConfig config = new MetricsConfig();
        config.setName(name);
        List<String> reporters = new ArrayList<>();
        reporters.add("default");
        config.setReporters(reporters).setEnabled(false);
        return config;
    }

    public void report(String name) {
        ApefMetric apefMetric = vexMetricMap.get(name);
        if (apefMetric == null) return;
        apefMetric.report();
    }

    public ApefMetric cacheMetric() {
        return this.cacheMetric;
    }

    public ApefMetric vexMetric(String name) {
        return this.vexMetricMap.computeIfAbsent(name,
                n -> new ApefMetric(createVexMetricConfig(n), this.reporterFactory));
    }

    public MetricItem<?> metricItem(String metricName, MetricItemType itemType, String itemName) {
        ApefMetric apefMetric = this.vexMetricMap.get(metricName);
        if (apefMetric == null) return null;
        return apefMetric.metricItem(itemType, itemName);
    }

    public void registerGuava(String cacheName, Cache cache) {
        Preconditions.checkNotNull(cache, "Cache instance can not be null");
        this.cacheMetric.metricRegistry().registerAll(GuavaMetrics.metricsFor(cacheName, cache));
    }

    public void registerLRUCache(String cacheName, LRUCache cache) {
        Preconditions.checkNotNull(cache, "Cache instance can not be null");
        this.cacheMetric.metricRegistry().registerAll(LRUCacheMetrics.metricsFor(cacheName, cache));
    }

    public void registerCaffeine(String cacheName, com.github.benmanes.caffeine.cache.Cache cache) {
        Preconditions.checkNotNull(cache, "Cache instance can not be null");
        this.cacheMetric.metricRegistry().registerAll(CaffeineMetrics.metricsFor(cacheName, cache));
    }

    public String toString() {
        return ObjectFormatter.toString(this.config);
    }
}
