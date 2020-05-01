package io.apef.sdef.connector.tcp;

import io.apef.base.config.factory.IConfigBase;
import io.apef.base.config.factory.XFactoryConfig;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class TcpServerConfig implements IConfigBase {
    private String name;
    private boolean enabled = true;
    private String host;
    private int port;

    public static class TcpServerFactoryConfig extends XFactoryConfig<TcpServerConfig> {

    }
}
