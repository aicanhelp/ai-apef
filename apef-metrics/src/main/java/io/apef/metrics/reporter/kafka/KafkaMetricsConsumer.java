package io.apef.metrics.reporter.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class KafkaMetricsConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaMetricsConsumer.class);
    private KafkaConsumer kafkaConsumer;
    private ObjectMapper mapper = new ObjectMapper();
    private AtomicBoolean stopped = new AtomicBoolean(false);

    public KafkaMetricsConsumer(String graphiteHost,
                                int graphitePort,
                                String zkConnect,
                                String topic,
                                String groupId,
                                int zkSessionTimeoutMs,
                                boolean readFromStartOfStream) {
        Properties props = new Properties();
        props.put("group.id", groupId);
        props.put("bootstrap.servers", zkConnect);
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        kafkaConsumer = new KafkaConsumer<>(props);
        kafkaConsumer.subscribe(Collections.singletonList(topic));

        log.info("Started consumer: topic=%s for zk=%s and groupId=%s".format(topic, zkConnect, groupId));
    }

    public void start() {
        new Thread(() -> {
            log.info("reading on stream now");
            ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                try {
                    KafkaMetricsReport report = mapper.readValue(record.value(), KafkaMetricsReport.class);
                    this.consumeReport(report);
                } catch (Throwable e) {
                    if (stopped.get()) {
                        log.info("Consumer worker has been stopped");
                        return;
                    } else {
                        log.warn("Error processing message, skipping this message: ", e);
                    }
                }
            }


        }).start();
    }

    protected abstract void consumeReport(KafkaMetricsReport report);

    public void stop() {
        log.info("Trying to stop consumer");
        kafkaConsumer.close();
        stopped.set(true);
        log.info("Consumer has been stopped");
    }
}
