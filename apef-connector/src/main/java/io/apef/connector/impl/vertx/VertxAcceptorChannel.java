package io.apef.connector.impl.vertx;

import io.apef.connector.acceptor.AcceptorChannel;
import io.vertx.ext.web.RoutingContext;

public class VertxAcceptorChannel extends AcceptorChannel<RoutingContext, RoutingContext> {
    public VertxAcceptorChannel(VertxAcceptorChannelConfig channelConfig) {
        super(channelConfig, new VertxAcceptorConnector(channelConfig));
    }
}
