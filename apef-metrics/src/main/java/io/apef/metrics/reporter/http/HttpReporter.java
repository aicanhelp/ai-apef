package io.apef.metrics.reporter.http;

import com.codahale.metrics.*;
import com.codahale.metrics.json.MetricsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

public class HttpReporter extends ScheduledReporter {
    private static final Logger log = LoggerFactory.getLogger(HttpReporter.class);
    private ObjectMapper mapper;
    private HttpClient httpClient;
    private String reportUrl;
    private MetricRegistry registry;

    public static Builder builder(MetricRegistry metricRegistry) {
        return new Builder(metricRegistry);
    }

    @Setter
    @Accessors(fluent = true)
    public static class Builder {
        private final MetricRegistry registry;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private VertxOptions vertxOptions;
        private HttpClientOptions httpClientOptions;
        private String reportUrl;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
        }

        public HttpReporter build() {
            Vertx vertx = Vertx.vertx(this.vertxOptions);
            HttpClient httpClient = vertx.createHttpClient(this.httpClientOptions);

            return new HttpReporter(registry,
                    httpClient,
                    this.filter,
                    rateUnit,
                    durationUnit, reportUrl);
        }
    }

    private HttpReporter(MetricRegistry registry, HttpClient httpClient,
                         MetricFilter filter,
                         TimeUnit rateUnit, TimeUnit durationUnit, String reportUrl) {
        super(registry, "Metrics-http-reporter", filter, rateUnit, durationUnit);
        this.httpClient = httpClient;
        this.reportUrl = reportUrl;
        this.registry = registry;
        mapper = new ObjectMapper().registerModule(new MetricsModule(rateUnit,
                durationUnit,
                false));
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        try {
            StringWriter report = new StringWriter();
            mapper.writeValue(report, this.registry);
            this.httpClient.post(this.reportUrl).end(report.toString());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
