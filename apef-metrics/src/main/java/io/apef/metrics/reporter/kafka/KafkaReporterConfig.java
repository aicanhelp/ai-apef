package io.apef.metrics.reporter.kafka;

import com.codahale.metrics.MetricRegistry;
import io.apef.base.utils.ObjectFormatter;
import io.apef.metrics.reporter.MetricsReporterConfig;
import io.apef.metrics.reporter.MetricsReporterType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class KafkaReporterConfig extends MetricsReporterConfig {
    @NotNull
    private String name = "kafka";
    private String brokerList;
    private String topic;
    private int batchSize = 100;
    private int compressionCodec = 0;
    private int sendMaxRetries = 3;
    private boolean sync = false;

    public KafkaReporterConfig() {
        super(MetricsReporterType.KAFKA);
    }

    @Override
    public KafkaReporter build(MetricRegistry metricRegistry) {
        return KafkaReporter.builder(metricRegistry, brokerList, topic)
                .batchSize(batchSize)
                .compressionCodec(compressionCodec)
                .durationUnit(getDurationUnit())
                .rateUnit(getRateUnit())
                .messageSendMaxRetries(sendMaxRetries)
                .synchronously(sync)
                .filter(filter())
                .build();
    }

    @Override
    public String toString() {
        return ObjectFormatter.toString(this);
    }
}