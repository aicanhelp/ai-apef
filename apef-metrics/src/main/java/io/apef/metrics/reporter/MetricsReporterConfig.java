package io.apef.metrics.reporter;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import io.apef.base.config.factory.XConfigBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public abstract class MetricsReporterConfig extends XConfigBase {
    protected static final Logger log = LoggerFactory.getLogger(MetricsReporterConfig.class);
    protected String filter;
    protected String prefix;
    protected TimeUnit durationUnit = TimeUnit.MINUTES;
    protected TimeUnit rateUnit = TimeUnit.SECONDS;
    protected int reportInterval = 3;
    protected TimeUnit reportIntervalUnit = TimeUnit.SECONDS;
    protected MetricsReporterType reporterType;
    private String name;

    protected MetricsReporterConfig(MetricsReporterType reporterType) {
        this.reporterType = reporterType;
    }

    public abstract ScheduledReporter build(MetricRegistry metricRegistry);

    protected MetricFilter filter() {
        return filter(this.filter);
    }

    protected static MetricFilter filter(String filterPattern) {
        MetricFilter filter = MetricFilter.ALL;
        if (StringUtils.isBlank(filterPattern)) return filter;
        Pattern pattern = Pattern.compile(filterPattern);

        return (name, metric) -> pattern.matcher(name).matches();
    }
}
