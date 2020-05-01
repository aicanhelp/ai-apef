package io.apef.core.channel.impl;

import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.ChannelHandler;
import io.apef.core.channel.message.ChannelInternalRequestMessage;
import io.apef.core.channel.pipe.ChannelPipeAcceptor;
import io.apef.core.channel.request.DefaultMessageType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FromBChannelPipeAcceptor implements ChannelPipeAcceptor {
    private BusinessChannel<?> srcChannel;

    @Override
    public <T, R> void response(ChannelInternalRequestMessage<T, R> message) {
        this.srcChannel.write(DefaultMessageType.Response, message);
    }

    @Override
    public <T, R> void accept(ChannelInternalRequestMessage<T, R> message,
                              ChannelHandler<T, R, ChannelInternalRequestMessage<T, R>> channelHandler) {
        channelHandler.handle(message);
    }
}
