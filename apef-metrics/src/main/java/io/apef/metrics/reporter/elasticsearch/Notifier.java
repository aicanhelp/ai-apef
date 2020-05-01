package io.apef.metrics.reporter.elasticsearch;


/**
 * A notifier interface, which is executed, in case a certain metric is matched on the percolation query
 */
public interface Notifier {

    /**
     * @param jsonMetric The json metric, which matched the percolation
     * @param matchedId  The name of the percolation id, that matched
     */
    void notify(JsonMetrics.JsonMetric jsonMetric, String matchedId);

}
