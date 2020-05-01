package io.apef.metrics.reporter.cassandra;

import com.codahale.metrics.MetricRegistry;
import io.apef.base.utils.ObjectFormatter;
import io.apef.metrics.reporter.MetricsReporterConfig;
import io.apef.metrics.reporter.MetricsReporterType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class CassandraReporterConfig extends MetricsReporterConfig {
    private List<String> addresses;
    private String keySpace;
    private String table;
    private int ttl;
    private int port;
    private String consistency;

    public CassandraReporterConfig() {
        super(MetricsReporterType.CASSANDRA);
    }

    @Override
    public CassandraReporter build(MetricRegistry metricRegistry) {
        Cassandra cassandra = new Cassandra(addresses, keySpace, table, ttl, port, consistency);
        cassandra.connect();
        return CassandraReporter.forRegistry(metricRegistry)
                .convertDurationsTo(getDurationUnit())
                .convertRatesTo(getRateUnit())
                .filter(filter())
                .prefixedWith(prefix)
                .build(cassandra);
    }

    @Override
    public String toString() {
        return ObjectFormatter.toString(this);
    }
}

