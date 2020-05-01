package io.apef.sdef.cluster;

import io.apef.sdef.cluster.k8s.K8SClusterConfig;
import io.apef.base.config.factory.XFactoryConfig;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
public class ApefClustersConfig {
    private Map<String, K8SClusterConfig> k8s = new HashMap<>();

    private VexClusterFactoryConfig factoryConfig;

    public VexClusterFactoryConfig factoryConfig() {
        if (factoryConfig == null) {
            factoryConfig = new VexClusterFactoryConfig();
            setConfigToFactory(k8s);
        }
        return factoryConfig;
    }

    private void setConfigToFactory(Map<String, ? extends ApefClusterConfig> configs) {
        configs.forEach((s, o) -> {
            if (factoryConfig.containsKey(s)) {
                throw new IllegalArgumentException("Cluster name must unique");
            }
            factoryConfig.put(s, o);
        });
    }

    public static class VexClusterFactoryConfig extends XFactoryConfig<ApefClusterConfig> {

    }
}
