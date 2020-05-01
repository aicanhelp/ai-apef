package io.apef.metrics.item;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

public class MetricItemTimer extends MetricItem<Timer> {

    public MetricItemTimer(MetricRegistry metricRegistry, String metricName, String itemName) {
        super(metricRegistry, metricRegistry::timer, metricName, MetricItemType.TIMER, itemName);
    }

    public Timer.Context start() {
        if (this.enabled())
            return this.metric().time();
        return null;
    }
}
