package io.apef.metrics.reporter.elasticsearch;

import com.codahale.metrics.MetricRegistry;
import io.apef.base.utils.ObjectFormatter;
import io.apef.metrics.reporter.MetricsReporterConfig;
import io.apef.metrics.reporter.MetricsReporterType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class ElasticSearchReporterConfig extends MetricsReporterConfig {
    private String name = "elasticSearch";
    private String hosts;
    private String index;
    private String indexDateFormat;
    private Map<String, Object> additionalFields;
    private int bulkSize;
    private String percolationFilter;
    private int timeout;
    private String timestampField;

    public ElasticSearchReporterConfig() {
        super(MetricsReporterType.ELASTICSEARCH);
    }

    @Override
    public ElasticsearchReporter build(MetricRegistry metricRegistry) {
        try {
            return ElasticsearchReporter.forRegistry(metricRegistry)
                    .hosts(hosts)
                    .index(index)
                    .indexDateFormat(indexDateFormat)
                    .additionalFields(additionalFields)
                    .bulkSize(bulkSize)
                    .percolationFilter(filter(percolationFilter))
                            //.percolationNotifier()
                    .prefixedWith(prefix)
                    .timeout(timeout)
                    .timestampFieldname(timestampField)
                    .convertDurationsTo(getDurationUnit())
                    .convertRatesTo(getRateUnit())
                    .filter(filter())
                    .build();
        } catch (Exception ex) {
            log.warn("Failed to create ElasticsearchReporter", ex);
        }
        return null;
    }

    @Override
    public String toString() {
        return ObjectFormatter.toString(this);
    }
}

