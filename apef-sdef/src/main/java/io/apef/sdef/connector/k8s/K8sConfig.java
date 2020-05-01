package io.apef.sdef.connector.k8s;

import io.apef.base.config.factory.IConfigBase;
import io.apef.base.config.factory.XFactoryConfig;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class K8sConfig implements IConfigBase {
    private String name;
    private boolean enabled = true;
    private String nameSpace;
    private String podName;
    private String k8sMasterUrl;
    private int port;
    private int updateInterval=2000;

    public static class K8sFactoryConfig extends XFactoryConfig<K8sConfig> {

    }
}
