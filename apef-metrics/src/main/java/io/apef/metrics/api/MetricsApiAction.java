package io.apef.metrics.api;

public enum MetricsApiAction {
    unknown,
    enable,
    disable,
    list,
    item,
    query,
    status;

    public static MetricsApiAction action(String action) {
        try {
            return MetricsApiAction.valueOf(action);
        } catch (Exception ex) {
            return unknown;
        }
    }
}
