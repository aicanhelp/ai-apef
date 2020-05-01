package io.apef.connector.acceptor;

import io.apef.core.channel.ServerChannelImpl;

/**
 * @param <IC> InStream Context
 * @param <OC> OutStream Context
 */
public class AcceptorChannel<IC, OC>
        extends ServerChannelImpl<AcceptorChannel<IC, OC>> {
    private AcceptorConnector<IC, OC> acceptorConnector;

    public AcceptorChannel(AcceptorChannelConfig channelConfig,
                           AcceptorConnector<IC, OC> acceptorConnector) {
        super(channelConfig);
        this.acceptorConnector = acceptorConnector;
        this.start();
    }

    public <T, R> AcceptorChannelContext<IC, OC, T, R> newChannelContext() {
        return new AcceptorChannelContext<>(this);
    }

    protected void registerChannelContext(AcceptorChannelContext channelContext) {
        this.acceptorConnector.getDispatcher().registerChannelContext(channelContext);
    }
}
