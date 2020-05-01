package io.apef.sdef.cluster.k8s;

import io.apef.sdef.cluster.ApefClusterConfig;
import io.apef.sdef.cluster.ApefClusterType;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class K8SClusterConfig implements ApefClusterConfig {
    private String name;
    private boolean enabled = true;
    private String k8sName;
    private String configNameSpace;

    @Override
    public ApefClusterType clusterType() {
        return ApefClusterType.K8S;
    }
}
