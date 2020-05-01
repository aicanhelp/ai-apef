package io.apef.core.channel.pipe;

import io.apef.core.channel.message.ChannelInternalRequestMessage;
import io.apef.core.channel.request.ChannelInternalRequest;

public interface ChannelInternalPipe
        extends B2BPipe, B2CPipe, S2BPipe, ChannelPipeContext {
    ChannelPipeAcceptor acceptor();

    ChannelPipeSender sender();

    boolean hasReply();

    void timeout(ChannelInternalRequestMessage message);

    void handleTimeout(ChannelInternalRequestMessage message);

    ChannelInternalRequest<? extends ChannelInternalRequest, Object, Object> request();

    ChannelInternalRequest<? extends ChannelInternalRequest, Object, Object> noReply();
}
