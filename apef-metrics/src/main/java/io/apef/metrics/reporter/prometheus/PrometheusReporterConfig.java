package io.apef.metrics.reporter.prometheus;

import com.codahale.metrics.MetricRegistry;
import io.apef.metrics.reporter.MetricsReporterConfig;
import io.apef.metrics.reporter.MetricsReporterType;
import lombok.Setter;

@Setter
public class PrometheusReporterConfig extends MetricsReporterConfig {
    private String prometheusAddress;

    protected PrometheusReporterConfig(MetricsReporterType reporterType) {
        super(reporterType);
    }

    @Override
    public PrometheusReporter build(MetricRegistry metricRegistry) {
        return PrometheusReporter.builder(metricRegistry)
                .rateUnit(getRateUnit())
                .prometheusAddress(this.prometheusAddress)
                .durationUnit(getDurationUnit())
                .build();
    }
}
