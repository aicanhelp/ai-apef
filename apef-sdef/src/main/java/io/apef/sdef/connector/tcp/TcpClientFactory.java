package io.apef.sdef.connector.tcp;

import io.apef.base.config.factory.XFactoryBase;

public class TcpClientFactory extends XFactoryBase<ITcpClient, TcpClientConfig> {
    public TcpClientFactory(TcpClientConfig.TcpClientFactoryConfig factoryConfig) {
        super(factoryConfig);
    }

    @Override
    protected ITcpClient newInstance(TcpClientConfig config) throws Exception {
        return null;
    }

    @Override
    protected void close(ITcpClient instance) {
        instance.close();
    }
}
