package io.apef.metrics.reporter.graphite;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.graphite.Graphite;
import com.codahale.metrics.graphite.GraphiteReporter;
import io.apef.base.utils.ObjectFormatter;
import io.apef.metrics.reporter.MetricsReporterConfig;
import io.apef.metrics.reporter.MetricsReporterType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.net.InetSocketAddress;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class GraphiteReporterConfig extends MetricsReporterConfig {
    private String name = "graphite";
    private String host;
    private int port;

    public GraphiteReporterConfig() {
        super(MetricsReporterType.GRAPHITE);
    }

    @Override
    public GraphiteReporter build(MetricRegistry metricRegistry) {
        final Graphite graphite = new Graphite(new InetSocketAddress(this.host, this.port));
        return GraphiteReporter.forRegistry(metricRegistry)
                .prefixedWith(prefix)
                .convertRatesTo(getRateUnit())
                .convertDurationsTo(getRateUnit())
                .filter(filter())
                .build(graphite);
    }

    @Override
    public String toString() {
        return ObjectFormatter.toString(this);
    }
}

