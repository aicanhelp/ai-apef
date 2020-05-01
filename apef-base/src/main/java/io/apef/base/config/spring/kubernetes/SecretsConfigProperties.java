package io.apef.base.config.spring.kubernetes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SecretsConfigProperties extends AbstractConfigProperties {

    private static final String TARGET = "Secret";

    private boolean enableApi = false;
    private Map<String, String> labels = new HashMap<>();
    private List<String> paths = new LinkedList<>();

    @Override
    public String getConfigurationTarget() {
        return TARGET;
    }
}
