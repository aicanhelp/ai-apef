package io.apef.metrics.reporter.kafka;

import com.codahale.metrics.*;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.kafka.clients.producer.KafkaProducer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

/**
 * A reference implementation of the KafkaReporter,
 * improve it especially on the serialization
 */
public class KafkaReporter extends ScheduledReporter {
    private static final Logger log = LoggerFactory.getLogger(KafkaReporter.class);

    private final KafkaProducer<String, String> kafkaProducer;
    private final String kafkaTopic;
    private final ObjectMapper mapper;
    private final MetricRegistry registry;

    private KafkaReporter(MetricRegistry registry,
                          String name,
                          MetricFilter filter,
                          TimeUnit rateUnit,
                          TimeUnit durationUnit,
                          String kafkaTopic,
                          Properties kafkaProperties) {
        super(registry, name, filter, rateUnit, durationUnit);
        this.registry = registry;
        mapper = new ObjectMapper().registerModule(new MetricsModule(rateUnit,
                durationUnit,
                false));
        this.kafkaTopic = kafkaTopic;
        kafkaProducer = new KafkaProducer<String, String>(kafkaProperties);
    }

    @Override
    public synchronized void report(SortedMap<String, Gauge> gauges,
                                    SortedMap<String, Counter> counters,
                                    SortedMap<String, Histogram> histograms,
                                    SortedMap<String, Meter> meters,
                                    SortedMap<String, Timer> timers) {
        try {
            log.info("Trying to report metrics to Kafka kafkaTopic {}", kafkaTopic);
            StringWriter report = new StringWriter();
            mapper.writeValue(report, registry);
            log.debug("Created metrics report: {}", report);
            kafkaProducer.send(new ProducerRecord<>(kafkaTopic, report.toString()));
            log.info("Metrics were successfully reported to Kafka kafkaTopic {}", kafkaTopic);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Builder builder(MetricRegistry registry, String brokerList, String kafkaTopic) {
        return new Builder(registry, kafkaTopic, brokerList);
    }

    @Setter
    @Getter
    @Accessors(fluent = true)
    public static class Builder {
        private MetricRegistry registry;
        private String kafkaTopic;
        private String brokerList;

        private boolean synchronously = false;
        private int compressionCodec = 0;
        private int batchSize = 200;
        private int messageSendMaxRetries = 3;

        private String name = "KafkaReporter";
        private MetricFilter filter = MetricFilter.ALL;
        private TimeUnit rateUnit = TimeUnit.SECONDS;
        private TimeUnit durationUnit = TimeUnit.SECONDS;

        public Builder(MetricRegistry registry, String topic, String brokerList) {
            this.registry = registry;
            this.kafkaTopic = topic;
            this.brokerList = brokerList;
        }

        public KafkaReporter build() {
            Properties props = new Properties();
            props.put("metadata.broker.list", brokerList);
            props.put("serializer.class", "kafka.serializer.StringEncoder");
            props.put("producer.type", synchronously ? "sync" : "reactive");
            props.put("compression.codec", String.valueOf(compressionCodec));
            props.put("batch.num.messages", String.valueOf(batchSize));
            props.put("message.send.max.retries", String.valueOf(messageSendMaxRetries));
            props.put("compression.codec", String.valueOf(compressionCodec));

            return new KafkaReporter(registry, name, filter, rateUnit, durationUnit, kafkaTopic, props);
        }
    }
}
