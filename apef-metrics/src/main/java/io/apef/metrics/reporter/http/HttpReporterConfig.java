package io.apef.metrics.reporter.http;

import com.codahale.metrics.MetricRegistry;
import io.apef.base.utils.ObjectFormatter;
import io.apef.metrics.reporter.MetricsReporterConfig;
import io.apef.metrics.reporter.MetricsReporterType;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClientOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class HttpReporterConfig extends MetricsReporterConfig {
    @NotNull
    private String name = "http";
    private VertxOptions vertxOptions;
    private HttpClientOptions httpClientOptions;
    private String reportUrl;

    public HttpReporterConfig() {
        super(MetricsReporterType.HTTP);
    }

    @Override
    public HttpReporter build(MetricRegistry metricRegistry) {
        return HttpReporter.builder(metricRegistry)
                .httpClientOptions(httpClientOptions)
                .vertxOptions(vertxOptions)
                .durationUnit(getDurationUnit())
                .rateUnit(getRateUnit())
                .filter(filter())
                .reportUrl(reportUrl)
                .build();
    }

    @Override
    public String toString() {
        return ObjectFormatter.toString(this);
    }
}
