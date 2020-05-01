package io.apef.core.channel.impl;

import io.apef.core.channel.Channel;
import io.apef.core.channel.ServerChannel;
import io.apef.core.channel.feature.timeout.TimeoutException;
import io.apef.core.channel.message.ChannelInternalRequestMessage;
import io.apef.core.channel.pipe.ChannelInternalPipe;
import io.apef.core.channel.request.ChannelInternalRequest;
import io.apef.core.channel.request.DefaultMessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public abstract class AbstractChannelPipeImpl implements ChannelInternalPipe {
    private Channel srcChannel;
    private Channel destChannel;

    @Override
    public void timeout(ChannelInternalRequestMessage message) {
        //use business channel to handle the timeout message
        if (srcChannel instanceof ServerChannel) {
            this.destChannel.write(DefaultMessageType.Timeout, message);
        } else {
            this.srcChannel.write(DefaultMessageType.Timeout, message);
        }
    }

    @Override
    public void handleTimeout(ChannelInternalRequestMessage message) {
        if (srcChannel instanceof ServerChannel) {
            message.fail(TimeoutException.INSTANCE.message(message.messageType(),
                    message.features().timeout().timeoutMs()),
                    TimeoutException.INSTANCE);
        } else {
            message.finishByTimeout();
        }
    }

    @Override
    public boolean hasReply() {
        return true;
    }

    @Override
    public ChannelInternalRequest<? extends ChannelInternalRequest, Object, Object> request() {
        return new ChannelMessageImpl<>(this);
    }

    @Override
    public ChannelInternalRequest<? extends ChannelInternalRequest, Object, Object> noReply() {
        return new ChannelMessageImpl<>(this);
    }

    @Override
    public ChannelInternalPipe channelPipe() {
        return this;
    }
}

