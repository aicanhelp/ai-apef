package io.apef.connector.impl.tcp;

import io.apef.connector.sender.SenderChannel;

public class TcpSenderChannel extends SenderChannel<TcpRequestContext> {
    public TcpSenderChannel(TcpSenderChannelConfig channelConfig) {
        super(channelConfig, new TcpSenderConnector(channelConfig));
    }
}
