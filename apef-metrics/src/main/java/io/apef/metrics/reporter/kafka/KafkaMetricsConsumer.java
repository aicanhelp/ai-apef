package io.apef.metrics.reporter.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import kafka.consumer.*;
import kafka.message.MessageAndMetadata;
import kafka.serializer.StringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class KafkaMetricsConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaMetricsConsumer.class);
    private ConsumerConnector consumerConnector;
    private KafkaStream<String, String> messageStream;
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
        props.put("zookeeper.connect", zkConnect);
        props.put("auto.offset.reset", readFromStartOfStream ? "smallest" : "largest");
        props.put("zookeeper.session.timeout.ms", String.valueOf(zkSessionTimeoutMs));

        ConsumerConfig config = new ConsumerConfig(props);
        consumerConnector = Consumer.create(config);
        TopicFilter filterSpec = new Whitelist(topic);

        log.info("Trying to start consumer: topic=%s for zk=%s and groupId=%s".format(topic, zkConnect, groupId));
        messageStream = consumerConnector.createMessageStreamsByFilter(filterSpec,
                1,
                new StringDecoder(null),
                new StringDecoder(null)).head();
        log.info("Started consumer: topic=%s for zk=%s and groupId=%s".format(topic, zkConnect, groupId));
    }

    public void start() {
        new Thread(() -> {
            log.info("reading on stream now");
            for (MessageAndMetadata<String, String> messageAndTopic : messageStream) {
                try {
                    KafkaMetricsReport report = mapper.readValue(messageAndTopic.message(), KafkaMetricsReport.class);
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
        consumerConnector.shutdown();
        stopped.set(true);
        log.info("Consumer has been stopped");
    }
}
