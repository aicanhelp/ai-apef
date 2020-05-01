package io.apef.connector.sender;

import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.MessageType;
import io.apef.core.channel.impl.FromBChannelPipeIml;

public class SenderChannelPipeImpl extends FromBChannelPipeIml implements SenderChannelPipe {
    SenderChannelPipeImpl(BusinessChannel srcChannel,
                          SenderChannel destChannel) {
        super(srcChannel, destChannel,
                new SenderChannelPipeInterceptor<>(destChannel.contextManager()));
    }

    @Override
    public <M extends SenderRequest<M, T, R>, T, R> M send(MessageType requestType) {
        return (M) SenderRequest.newRequest(this).requestType(requestType);
    }

    @Override
    public <M extends SenderRequest<M, T, R>, T, R> M send() {
        return (M) SenderRequest.newRequest(this);
    }
}
