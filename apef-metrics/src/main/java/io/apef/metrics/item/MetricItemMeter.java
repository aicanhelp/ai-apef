package io.apef.metrics.item;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

public class MetricItemMeter extends MetricItem<Meter> {

    public MetricItemMeter(MetricRegistry metricRegistry, String metricName, String itemName) {
        super(metricRegistry, metricRegistry::meter, metricName, MetricItemType.METER, itemName);
    }

    public void mark() {
        if (this.enabled()) {
            this.metric().mark();
        }
    }

    public void mark(long n) {
        if (this.enabled()) {
            this.metric().mark(n);
        }
    }
}
