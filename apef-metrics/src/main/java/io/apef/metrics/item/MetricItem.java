package io.apef.metrics.item;

import com.codahale.metrics.Counting;
import com.codahale.metrics.MetricRegistry;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public abstract class MetricItem<T extends Counting> {
    private Register<T> register;
    private MetricRegistry metricRegistry;
    private String name;
    private T metric;
    private String metricName;
    private String itemName;
    private MetricItemType itemType;
    private boolean enabled = false;

    MetricItem(MetricRegistry metricRegistry,
               Register<T> register,
               String metricName, MetricItemType itemType,
               String itemName) {
        this.metricRegistry = metricRegistry;
        this.register = register;
        this.metricName = metricName;
        this.itemType = itemType;
        this.itemName = itemName;
        this.name = metricName + "_" + itemName + "_" + this.itemType;
    }

    public String name() {
        return this.name;
    }

    public MetricItem<T> enabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) this.metricRegistry.remove(name);
        else {
            if (this.metric == null) {
                this.metric = this.register.register(name);
            }
        }
        return this;
    }

    public long count() {
        if (this.metric == null) return 0;
        return this.metric.getCount();
    }

    protected interface Register<T> {
        T register(String name);
    }
}
