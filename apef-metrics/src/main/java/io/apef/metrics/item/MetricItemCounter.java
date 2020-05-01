package io.apef.metrics.item;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

public class MetricItemCounter extends MetricItem<Counter> {

    public MetricItemCounter(MetricRegistry metricRegistry,
                      String metricName, String itemName) {
        super(metricRegistry, metricRegistry::counter, metricName, MetricItemType.COUNTER, itemName);
    }

    public void inc() {
        if (this.enabled())
            this.metric().inc();
    }
}
