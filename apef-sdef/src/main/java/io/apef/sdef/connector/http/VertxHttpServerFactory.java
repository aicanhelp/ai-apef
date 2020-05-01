package io.apef.sdef.connector.http;

import io.apef.base.config.factory.XFactoryBase;

import io.apef.sdef.connector.http.server.VertxHttpServer;
import io.apef.sdef.connector.http.server.VertxHttpServerConfig;
import io.vertx.core.Vertx;

public class VertxHttpServerFactory extends XFactoryBase<VertxHttpServer, VertxHttpServerConfig> {
    private Vertx vertx;

    public VertxHttpServerFactory(Vertx vertx,
                                  VertxHttpServerConfig.VertxServerFactoryConfig factoryConfig) {
        super(factoryConfig);
        this.vertx = vertx;
    }

    @Override
    protected VertxHttpServer newInstance(VertxHttpServerConfig config) throws Exception {
        return new VertxHttpServer(vertx, config)
                .start();
    }

    @Override
    protected void close(VertxHttpServer instance) {
        if (instance != null)
            instance.close();
    }

}
