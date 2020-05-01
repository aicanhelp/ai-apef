package io.apef.core.channel;

import io.apef.core.channel.message.ChannelMessageContext;

public interface ChannelHandler2<T, R> {
    void handle(ChannelMessageContext<R> messageContext, T requestContent);
}
