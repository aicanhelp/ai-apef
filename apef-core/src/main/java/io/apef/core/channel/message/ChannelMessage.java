package io.apef.core.channel.message;

import io.apef.core.channel.MessageType;

public interface ChannelMessage<T, R> {
    MessageType messageType();

    T requestContent();

    ChannelMessageContext<R> messageContext();
}
