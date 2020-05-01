package io.apef.metrics.reporter;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class ApefMetricsReporter {
    private MetricRegistry metricRegistry;
    private MetricsReporterConfig reporterConfig;
    private ScheduledReporter reporter;

    public ApefMetricsReporter(MetricRegistry metricRegistry, MetricsReporterConfig reporterConfig) {
        this.metricRegistry = metricRegistry;
        this.reporterConfig = reporterConfig;
    }

    public void start() {
        if (this.reporter == null)
            this.reporter = this.reporterConfig.build(this.metricRegistry);
        if (this.reporter == null) {
            throw new IllegalArgumentException("Failed to create new Metrics Reporter with config: " + this.reporterConfig);
        }

        this.reporter.start(this.reporterConfig.getReportInterval(), this.reporterConfig.getReportIntervalUnit());
    }

    public void stop() {
        if (this.reporter == null) return;
        this.reporter.stop();
    }

    public void report() {
        if (this.reporter == null) return;
        this.reporter.report();
    }
}
