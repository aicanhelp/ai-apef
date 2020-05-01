package io.apef.metrics.reporter.mongo;

import com.codahale.metrics.MetricRegistry;
import io.apef.metrics.reporter.MetricsReporterConfig;
import io.apef.metrics.reporter.MetricsReporterType;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import io.apef.base.utils.ObjectFormatter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.stream.Stream;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class MongoReporterConfig extends MetricsReporterConfig {
    @NotNull
    private String name = "mongo";
    @NotBlank
    private String serverAddresses;
    private MongoClientOptions mongoClientOptions;
    @NotBlank
    private String databaseName;
    private MongoCredential[] mongoCredentials;

    public MongoReporterConfig() {
        super(MetricsReporterType.MONGO);
    }

    @Override
    public MongoDBReporter build(MetricRegistry metricRegistry) {
        return MongoDBReporter.forRegistry(metricRegistry)
                .convertDurationsTo(getDurationUnit())
                .convertRatesTo(getRateUnit())
                .filter(filter())
                .prefixedWith(prefix)
                .serverAddresses(serverAddresses())
                .mongoClientOptions(mongoClientOptions)
                .withDatabaseName(databaseName)
                .mongoCredentials(mongoCredentials)
                        //.additionalFields()
                .build();
    }

    private ServerAddress[] serverAddresses() {
        return Stream.of(StringUtils.split(this.serverAddresses, ','))
                .map(s -> {
                    String[] hostPort = StringUtils.split(s, ':');
                    try {
                        return new ServerAddress(hostPort[0], Integer.parseInt(hostPort[1]));
                    } catch (Exception ex) {
                        return new ServerAddress(hostPort[0]);
                    }
                }).toArray(ServerAddress[]::new);

    }

    @Override
    public String toString() {
        return ObjectFormatter.toString(this);
    }
}
