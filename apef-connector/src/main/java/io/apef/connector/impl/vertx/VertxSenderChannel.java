package io.apef.connector.impl.vertx;

import io.apef.connector.sender.SenderChannel;

public class VertxSenderChannel extends SenderChannel<VertxRequestContext> {
    public VertxSenderChannel(VertxSenderChannelConfig channelConfig) {
        super(channelConfig, new VertxSenderConnector(channelConfig));
    }
}
