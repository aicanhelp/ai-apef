package io.apef.metrics.api;


import io.apef.metrics.ApefMetricsFactory;
import io.apef.metrics.config.MetricsApisConfig;
import io.apef.metrics.item.MetricItem;
import io.apef.metrics.item.MetricItemType;

import java.util.List;

public class MetricsApi {

    public static MetricsApisConfig apisContext() {
        return ApefMetricsFactory.config().getMetricsApis();
    }

    public static void enable(String metricName,
                              MetricItemType itemType,
                              String itemName) {
        ApefMetricsFactory.defaultMetrics().metricItem(metricName, itemType, itemName).enabled();
    }

    public static void disable(String metricName,
                               MetricItemType itemType,
                               String opName) {
        ApefMetricsFactory.defaultMetrics().metricItem(metricName, itemType, opName).enabled(false);
    }

    //todo
    public static List<MetricItem> allItems() {
        return null;
    }

    //todo
    public static List<MetricItem> allItems(String metricName) {
        return null;
    }

    //todo
    public static MetricItem item(String metricName,
                                  MetricItemType itemType,
                                  String itemName) {
        return null;
    }

    //todo
    public static List<MetricItem> query(String metricName,
                                         MetricItemType itemType,
                                         String itemName) {
        return null;
    }
}
