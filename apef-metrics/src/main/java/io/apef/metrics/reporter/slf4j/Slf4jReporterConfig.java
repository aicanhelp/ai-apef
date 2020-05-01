package io.apef.metrics.reporter.slf4j;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import io.apef.base.utils.ObjectFormatter;
import io.apef.metrics.reporter.MetricsReporterConfig;
import io.apef.metrics.reporter.MetricsReporterType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.slf4j.LoggerFactory;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class Slf4jReporterConfig extends MetricsReporterConfig {
    private String logger = "ROOT";

    public Slf4jReporterConfig() {
        super(MetricsReporterType.SLF4j);
    }

    public Slf4jReporter build(MetricRegistry metricRegistry) {
        if (!this.isEnabled()) return null;

        return Slf4jReporter.forRegistry(metricRegistry)
                .convertDurationsTo(getDurationUnit())
                .convertRatesTo(getRateUnit())
                .filter(filter())
                .outputTo(LoggerFactory.getLogger(this.getLogger()))
                .build();
    }

    @Override
    public String toString() {
        return ObjectFormatter.toString(this);
    }
}

