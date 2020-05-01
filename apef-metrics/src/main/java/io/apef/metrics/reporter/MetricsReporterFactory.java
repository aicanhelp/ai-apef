package io.apef.metrics.reporter;

import com.codahale.metrics.MetricRegistry;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class MetricsReporterFactory {
    private MetricsReportersConfig reportersConfig;
    private Map<MetricRegistry, Map<String, ApefMetricsReporter>> reporterMap = new HashMap<>();

    public MetricsReporterFactory(MetricsReportersConfig reportersConfig) {
        this.reportersConfig = reportersConfig;
        reportersConfig.validate();
    }

    public ApefMetricsReporter reporter(String reporterName, MetricRegistry metricRegistry) {
        assert !StringUtils.isEmpty(reporterName);
        assert metricRegistry != null;
        Map<String, ApefMetricsReporter> scheduledReporterMap = this.reporterMap.get(metricRegistry);
        if (scheduledReporterMap == null) return null;
        return scheduledReporterMap.get(reporterName);
    }

    public synchronized ApefMetricsReporter startReporter(String reporterName, MetricRegistry metricRegistry) {
        assert !StringUtils.isEmpty(reporterName);
        assert metricRegistry != null;
        Map<String, ApefMetricsReporter> scheduledReporterMap =
                this.reporterMap.computeIfAbsent(metricRegistry, metricRegistry1 -> new HashMap<>());
        ApefMetricsReporter reporter = scheduledReporterMap.get(reporterName);
        if (reporter != null) return reporter;
        MetricsReporterConfig reporterConfig = this.reportersConfig.metricsReporter(reporterName);
        if (reporterConfig == null) {
            throw new IllegalArgumentException("Can not find reporter with name: " + reporterName);
        }
        reporter = new ApefMetricsReporter(metricRegistry, reporterConfig);
        scheduledReporterMap.put(reporterName, reporter);

        if (reporter.reporterConfig().isEnabled()) {
            reporter.start();
        }
        return reporter;
    }

    public void stopReporter(String reporterName, MetricRegistry metricRegistry) {
        ApefMetricsReporter reporter = reporter(reporterName, metricRegistry);
        if (reporter == null) return;
        reporter.stop();
    }
}
