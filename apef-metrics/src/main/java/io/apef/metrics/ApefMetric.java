package io.apef.metrics;

import com.codahale.metrics.MetricRegistry;
import io.apef.base.utils.ObjectFormatter;
import io.apef.metrics.config.MetricsConfig;
import io.apef.metrics.item.*;
import io.apef.metrics.reporter.MetricsReporterFactory;
import io.apef.metrics.reporter.ApefMetricsReporter;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

public class ApefMetric {
    @Getter
    @Accessors(fluent = true)
    private MetricsConfig metricsConfig;
    @Getter
    @Accessors(fluent = true)
    private MetricRegistry metricRegistry;
    MetricsReporterFactory reporterFactory;

    private Map<String, MetricItemCounter> counters = new HashMap<>();
    private Map<String, MetricItemTimer> timers = new HashMap<>();
    private Map<String, MetricItemMeter> meters = new HashMap<>();
    private Map<String, MetricItemHistogram> histograms = new HashMap<>();

    protected ApefMetric(MetricsConfig metricsConfig, MetricsReporterFactory reporterFactory) {
        this.metricsConfig = metricsConfig;
        this.reporterFactory = reporterFactory;
        this.metricRegistry = new MetricRegistry();
        this.bindReporter();
    }

    public MetricItem metricItem(MetricItemType itemType, String name) {
        switch (itemType) {
            case COUNTER:
                return counter(name);
            case TIMER:
                return timer(name);
            case METER:
                return meter(name);
            case HISTOGRAM:
                return histogram(name);
        }
        return null;
    }

    private boolean enabled(MetricItemType itemType, String itemName) {
        if (!this.metricsConfig.isEnabled()) return false;
        switch (itemType) {
            case HISTOGRAM:
                return this.metricsConfig.getHistograms().getOrDefault(itemName, false);
            case COUNTER:
                return this.metricsConfig.getCounters().getOrDefault(itemName, false);
            case METER:
                return this.metricsConfig.getMeters().getOrDefault(itemName, false);
            case TIMER:
                return this.metricsConfig.getTimers().getOrDefault(itemName, false);
        }

        return false;
    }

    public void enable(boolean enable) {
        this.metricsConfig.setEnabled(enable);
        this.counters.forEach((s, metricItemCounter) -> metricItemCounter.enabled(enable));
        this.timers.forEach((s, metricItemTimer) -> metricItemTimer.enabled(enable));
        this.histograms.forEach((s, metricItemHistogram) -> metricItemHistogram.enabled(enable));
        this.counters.forEach((s, metricItemCounter) -> metricItemCounter.enabled(enable));
    }

    private <T extends MetricItem> T setDefaultEnabled(T metricItem) {
        metricItem.enabled(enabled(metricItem.itemType(), metricItem.itemName()));
        return metricItem;
    }

    public synchronized MetricItemCounter counter(String name) {
        return counters.computeIfAbsent(name,
                s -> setDefaultEnabled(new MetricItemCounter(this.metricRegistry, this.metricsConfig.getName(), name)));
    }

    public synchronized MetricItemTimer timer(String name) {
        return timers.computeIfAbsent(name,
                s -> setDefaultEnabled(new MetricItemTimer(this.metricRegistry, this.metricsConfig.getName(), name)));
    }

    public synchronized MetricItemMeter meter(String name) {
        return meters.computeIfAbsent(name,
                s -> setDefaultEnabled(new MetricItemMeter(this.metricRegistry, this.metricsConfig.getName(), name)));
    }

    public synchronized MetricItemHistogram histogram(String name) {
        return histograms.computeIfAbsent(name,
                s -> setDefaultEnabled(new MetricItemHistogram(this.metricRegistry, this.metricsConfig.getName(), name)));
    }

    private void bindReporter() {
        this.metricsConfig().getReporters().forEach(s1 -> {
            reporterFactory.startReporter(s1, this.metricRegistry());
        });
    }

    public void report() {
        this.metricsConfig().getReporters().forEach(s1 -> {
            reporterFactory.reporter(s1, metricRegistry).report();
        });
    }

    public void report(String reportName) {
        ApefMetricsReporter reporter = reporterFactory.reporter(reportName, metricRegistry);
        if (reporter == null) {
            throw new IllegalArgumentException("Can not find reporter of name:" + reportName);
        }
        reporter.report();
    }

    public String toString() {
        return ObjectFormatter.toString(this);
    }
}
