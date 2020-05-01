package io.apef.metrics.ext;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractExtMetrics extends HashMap<String, Metric> implements MetricSet {
    @Override
    public Map<String, Metric> getMetrics() {
        return this;
    }
}
