package io.apef.metrics.config;

import io.apef.metrics.reporter.MetricsReportersConfig;
import lombok.Data;

import javax.validation.Valid;

@Data
public class ApefMetricsConfig {
    @Valid
    MetricsApisConfig metricsApis=new MetricsApisConfig();
    MetricsReportersConfig reporters=new MetricsReportersConfig();
    MetricsFactoryConfig metricsFactory = new MetricsFactoryConfig();

}
