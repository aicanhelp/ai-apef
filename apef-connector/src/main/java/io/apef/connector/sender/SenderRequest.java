package io.apef.connector.sender;

import io.apef.core.channel.MessageType;
import io.apef.core.channel.pipe.ChannelPipeContext;
import io.apef.core.channel.request.B2CRequest;

public interface SenderRequest<M extends SenderRequest<M, T, R>,
        T, R> extends B2CRequest<M, T, R> {
    M ignoreCache(boolean ignoreCache);

    M requestType(MessageType requestType);

    static <T, R> SenderRequest<?, T, R> newRequest() {
        return new SenderRequestContext<>();
    }

    static <T, R> SenderRequest<?, T, R> newRequest(ChannelPipeContext channelPipeContext) {
        return new SenderRequestContext<>(channelPipeContext);
    }
}
