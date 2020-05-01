package io.apef.core.channel;

import io.apef.core.channel.message.ChannelMessage;

public interface ChannelHandler<T, R, M extends ChannelMessage<T, R>> {
    void handle(M requestMessage);
}
