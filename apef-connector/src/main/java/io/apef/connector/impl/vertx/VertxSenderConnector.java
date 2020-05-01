package io.apef.connector.impl.vertx;

import io.apef.sdef.ApefSdef;
import io.apef.connector.sender.SenderConnector;
import io.apef.connector.sender.SenderRequestContext;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VertxSenderConnector extends SenderConnector<VertxRequestContext> {
    private VertxSenderChannelConfig config;
    private HttpClient httpClient;

    public VertxSenderConnector(VertxSenderChannelConfig channelConfig) {
        this.config = channelConfig;
        this.httpClient = ApefSdef.httpClient(channelConfig.getVertxClient());
    }

    private HttpClientRequest clientRequest(String url) {
        if (this.config.isPost())
            return this.httpClient.postAbs(url);
        else
            return this.httpClient.getAbs(url);
    }

    @Override
    protected VertxRequestContext newConnectorContext(SenderRequestContext<VertxRequestContext, ?, ?, ?> requestContext) {
        HttpClientRequest clientRequest = clientRequest(this.config.getServiceAddress());
        return new VertxRequestContext(clientRequest, requestContext);
    }
}
