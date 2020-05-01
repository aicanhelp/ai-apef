package io.apef.metrics.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.NotBlank;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class MetricsApisConfig {
    @NotBlank
    private String rootContext = "/metrics";
}
