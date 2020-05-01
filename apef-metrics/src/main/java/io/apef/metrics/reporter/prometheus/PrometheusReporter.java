package io.apef.metrics.reporter.prometheus;

import com.codahale.metrics.*;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.PushGateway;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.SortedMap;
import java.util.concurrent.TimeUnit;


public class PrometheusReporter extends ScheduledReporter {

    private String prometheusAddress;
    private CollectorRegistry collectorRegistry = new CollectorRegistry();

    protected PrometheusReporter(MetricRegistry registry, String prometheusAddress,
                                 MetricFilter filter,
                                 TimeUnit rateUnit, TimeUnit durationUnit) {
        super(registry, "PrometheusReporter", filter, rateUnit, durationUnit);
        new DropwizardExports(registry).register(collectorRegistry);
    }

    public static Builder builder(MetricRegistry metricRegistry) {
        return new Builder(metricRegistry);
    }

    @Setter
    @Accessors(fluent = true)
    public static class Builder {
        private final MetricRegistry registry;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private String prometheusAddress;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
        }

        public PrometheusReporter build() {
            return new PrometheusReporter(registry,
                    this.prometheusAddress,
                    this.filter,
                    rateUnit,
                    durationUnit);
        }
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        PushGateway pg = new PushGateway(this.prometheusAddress);
        try {
            pg.push(this.collectorRegistry, "PrometheusReporter");
        } catch (Exception ex) {

        }
    }
}
