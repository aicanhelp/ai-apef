package io.apef.connector.impl.tcp;

import io.apef.sdef.connector.tcp.ITcpContext;
import io.apef.connector.acceptor.AcceptorChannel;

public class TcpAcceptorChannel extends AcceptorChannel<ITcpContext, ITcpContext> {
    public TcpAcceptorChannel(TcpAcceptorChannelConfig channelConfig) {
        super(channelConfig, new TcpAcceptorConnector(channelConfig));
    }
}
