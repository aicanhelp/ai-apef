package io.apef.metrics.reporter.ganglia;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ganglia.GangliaReporter;
import io.apef.base.utils.ObjectFormatter;
import io.apef.metrics.reporter.MetricsReporterConfig;
import io.apef.metrics.reporter.MetricsReporterType;
import info.ganglia.gmetric4j.gmetric.GMetric;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class GangliaReporterConfig extends MetricsReporterConfig {
    private String name = "ganglia";
    private String group;
    private int port;
    private GMetric.UDPAddressingMode mode = GMetric.UDPAddressingMode.MULTICAST;
    private int ttl;
    private boolean version31 = true;
    private String uuid;
    private int tMax = 60, dMax = 0;

    public GangliaReporterConfig() {
        super(MetricsReporterType.GANGLIA);
    }

    @Override
    public GangliaReporter build(MetricRegistry metricRegistry) {
        try {
            GMetric ganglia = new GMetric(this.group, this.port, this.mode,
                    this.ttl, this.version31, UUID.fromString(this.uuid));
            return GangliaReporter.forRegistry(metricRegistry)
                    .convertRatesTo(getRateUnit())
                    .convertDurationsTo(getDurationUnit())
                    .prefixedWith(prefix)
                    .withDMax(this.dMax)
                    .withTMax(this.tMax)
                    .filter(filter())
                    .build(ganglia);
        } catch (Exception ex) {

        }
        return null;
    }

    @Override
    public String toString() {
        return ObjectFormatter.toString(this);
    }
}

