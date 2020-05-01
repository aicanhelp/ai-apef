package io.apef.metrics.item;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;

public class MetricItemHistogram extends MetricItem<Histogram> {

    public MetricItemHistogram(MetricRegistry metricRegistry, String metricName, String itemName) {
        super(metricRegistry, metricRegistry::histogram, metricName, MetricItemType.HISTOGRAM, itemName);
    }

    public void update(int value) {
        if (this.enabled()) {
            this.metric().update(value);
        }
    }

    public void update(long value) {
        if (this.enabled()) {
            this.metric().update(value);
        }
    }
}
