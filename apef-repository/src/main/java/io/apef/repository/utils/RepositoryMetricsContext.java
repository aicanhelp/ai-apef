package io.apef.repository.utils;

import io.apef.metrics.ApefMetric;
import io.apef.metrics.ApefMetricsFactory;
import io.apef.metrics.item.MetricItemTimer;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class RepositoryMetricsContext {
    private ApefMetric metric;
    private MetricItemTimer getMetric, saveMetric, existMetric, getAllMetric, putAllMetric;

    public RepositoryMetricsContext(String name) {
        this.metric = ApefMetricsFactory.defaultMetrics()
                .vexMetric(name);
        this.getMetric = this.metric.timer("get");
        this.saveMetric = this.metric.timer("save");
        this.existMetric = this.metric.timer("exists");
        this.getAllMetric = this.metric.timer("getAll");
        this.putAllMetric = this.metric.timer("putAll");
    }
}
