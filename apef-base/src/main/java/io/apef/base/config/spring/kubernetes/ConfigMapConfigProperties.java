package io.apef.base.config.spring.kubernetes;


public class ConfigMapConfigProperties extends AbstractConfigProperties {
    private static final String TARGET = "Config Map";

    @Override
    public String getConfigurationTarget() {
        return TARGET;
    }
}
