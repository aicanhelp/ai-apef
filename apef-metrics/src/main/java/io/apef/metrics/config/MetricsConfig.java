package io.apef.metrics.config;

import io.apef.base.config.factory.XConfigBase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class MetricsConfig extends XConfigBase {
    private List<String> reporters = new ArrayList<>();
    private Map<String, Boolean> timers = new HashMap<>();
    private Map<String, Boolean> counters = new HashMap<>();
    private Map<String, Boolean> meters = new HashMap<>();
    private Map<String, Boolean> histograms = new HashMap<>();
}
