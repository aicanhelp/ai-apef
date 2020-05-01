package io.apef.sdef.connector.http;

import io.apef.base.config.factory.XFactoryBase;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;

public class VertxHttpClientFactory extends XFactoryBase<HttpClient, VertxHttpClientConfig> {
    private Vertx vertx;

    public VertxHttpClientFactory(Vertx vertx,
                                  VertxHttpClientConfig.VertxHttpClientFactoryConfig factoryConfig) {
        super(factoryConfig);
        this.vertx = vertx;
    }

    @Override
    protected HttpClient newInstance(VertxHttpClientConfig config) throws Exception {
        try {
            return vertx.createHttpClient(config);
        } catch (Exception ex) {

            log.error("", ex);
            throw ex;
        }
    }

    @Override
    protected void close(HttpClient instance) {
        if (instance != null)
            instance.close();
    }
}
