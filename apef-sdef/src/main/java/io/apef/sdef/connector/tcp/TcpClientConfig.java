package io.apef.sdef.connector.tcp;

import io.apef.base.config.factory.IConfigBase;
import io.apef.base.config.factory.XFactoryConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class TcpClientConfig implements IConfigBase {
    private String name;
    private boolean enabled = true;
    private String k8sName;

    public static class TcpClientFactoryConfig extends XFactoryConfig<TcpClientConfig> {

    }
}
