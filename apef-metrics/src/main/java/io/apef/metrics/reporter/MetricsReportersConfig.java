package io.apef.metrics.reporter;

import io.apef.base.config.factory.XCompositeConfig;
import io.apef.base.utils.ObjectFormatter;
import io.apef.metrics.reporter.cassandra.CassandraReporterConfig;
import io.apef.metrics.reporter.elasticsearch.ElasticSearchReporterConfig;
import io.apef.metrics.reporter.http.HttpReporterConfig;
import io.apef.metrics.reporter.kafka.KafkaReporterConfig;
import io.apef.metrics.reporter.mongo.MongoReporterConfig;
import io.apef.metrics.reporter.prometheus.PrometheusReporterConfig;
import io.apef.metrics.reporter.slf4j.Slf4jReporterConfig;
import io.apef.metrics.reporter.graphite.GraphiteReporterConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class MetricsReportersConfig extends XCompositeConfig {
    private Map<String, Slf4jReporterConfig> slf4j = new HashMap<>();
    private Map<String, GraphiteReporterConfig> graphite = new HashMap<>();
    private Map<String, CassandraReporterConfig> cassandra = new HashMap<>();
    private Map<String, ElasticSearchReporterConfig> elasticSearch = new HashMap<>();
    private Map<String, HttpReporterConfig> http = new HashMap<>();
    private Map<String, KafkaReporterConfig> kafka = new HashMap<>();
    private Map<String, MongoReporterConfig> mongo = new HashMap<>();
    private Map<String, PrometheusReporterConfig> prometheus = new HashMap<>();

    private Map<String, MetricsReporterConfig> all = new HashMap<>();

    public void validate() {
        this.setToAll(this.slf4j)
                .setToAll(this.graphite)
                .setToAll(this.cassandra)
                .setToAll(this.elasticSearch)
                .setToAll(this.http)
                .setToAll(this.kafka)
                .setToAll(this.mongo)
                .setToAll(this.prometheus);
        if(!all.containsKey("default")){
            this.all.put("default",createDefaultReporter());
        }
    }

    private MetricsReporterConfig createDefaultReporter(){
        return new Slf4jReporterConfig()
                .setName("default")
                .setDurationUnit(TimeUnit.MINUTES)
                .setRateUnit(TimeUnit.SECONDS);
    }

    private MetricsReportersConfig setToAll(Map<String, ? extends MetricsReporterConfig> configMap) {
        configMap.forEach((s, o) -> {
            if (all.containsKey(s)) {
                throw new IllegalArgumentException("The metrics reporter name must be unique: " + s);
            }

            all.put(s, o.setName(s));
        });
        return this;
    }

    public MetricsReporterConfig metricsReporter(String name) {
        return this.all.get(name);
    }

    @Override
    public String toString() {
        return ObjectFormatter.toString(this);
    }
}
