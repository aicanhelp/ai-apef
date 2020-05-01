package io.apef.metrics;

import io.apef.base.config.utils.ConfigurationLoader;
import io.apef.base.config.validation.ConfigValidator;
import io.apef.metrics.config.ApefMetricsConfig;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ApefMetricsFactory {

    private final static String configFile = "apef-metrics-config.xml";

    private static class InstanceHolder {
        static ApefMetricsFactory instance = instance();

        static ApefMetricsFactory instance() {
            ApefMetricsConfig apefMetricsConfig
                    = ConfigurationLoader.loadConfiguration(ApefMetricsConfig.class,
                    configFile);
            ConfigValidator.validate(apefMetricsConfig);
            return new ApefMetricsFactory(apefMetricsConfig);
        }
    }

    private ApefMetricsConfig apefMetricsConfig;
    private ApefMetrics vexMetrics;

    private ApefMetricsFactory(ApefMetricsConfig apefMetricsConfig) {
        ConfigValidator.validate(apefMetricsConfig);
        this.apefMetricsConfig = apefMetricsConfig;
        this.vexMetrics = new ApefMetrics(apefMetricsConfig);
    }

    public static ApefMetricsConfig config() {
        return InstanceHolder.instance().apefMetricsConfig;
    }

    /**
     * Currently, we only have a default metrics
     *
     * @return
     */
    public static ApefMetrics defaultMetrics() {
        return InstanceHolder.instance.vexMetrics;
    }

    /**
     * Currently, we only have a default metrics
     *
     * @return
     */
    public static ApefMetrics metrics(String name) {
        return InstanceHolder.instance.vexMetrics;
    }
}
