package io.apef.connector.sender;

import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.ClientChannelImpl;
import io.apef.connector.base.ConnectorChannelContextManager;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class SenderChannel<C extends ConnectorRequestContext<C>> extends ClientChannelImpl<SenderChannel<C>> {
    private SenderConnector<C> senderConnector;

    private ConnectorChannelContextManager<SenderChannelContext>
            contextManager;

    public SenderChannel(SenderChannelConfig channelConfig,
                         SenderConnector<C> senderConnector) {
        super(channelConfig);
        this.senderConnector = senderConnector;
        this.contextManager = new ConnectorChannelContextManager<>(
                channelConfig.getName() + "_SenderChannelContextManager",
                SenderChannelContext[]::new
        );

        //ChannelPipeInterceptor will check the cache firstly,
        //If not hit cache, then handle the request
        super.handler(SenderRequestMessage.TYPE, this::handleRequest).start();
    }

    public SenderChannelPipe senderChannelPipe(BusinessChannel businessChannel) {
        return new SenderChannelPipeImpl(businessChannel, this);
    }

    public <K, T, R> SenderChannelContext<C, K, T, R> newChannelContext() {
        return new SenderChannelContext<>(this);
    }

    protected void registerChannelContext(SenderChannelContext senderChannelContext) {
        this.contextManager.registerChannelContext(senderChannelContext);
    }

    private void handleRequest(SenderRequestContext<C, Object, Object, Object> message) {
        this.senderConnector.accept(message);
    }
}
