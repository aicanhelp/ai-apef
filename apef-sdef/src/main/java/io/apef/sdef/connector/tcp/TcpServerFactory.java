package io.apef.sdef.connector.tcp;

import io.apef.base.config.factory.XFactoryBase;

public class TcpServerFactory extends XFactoryBase<ITcpServer, TcpServerConfig> {
    public TcpServerFactory(TcpServerConfig.TcpServerFactoryConfig factoryConfig) {
        super(factoryConfig);
    }

    @Override
    protected ITcpServer newInstance(TcpServerConfig config) throws Exception {
        return null;
    }

    @Override
    protected void close(ITcpServer instance) {
        instance.close();
    }
}
