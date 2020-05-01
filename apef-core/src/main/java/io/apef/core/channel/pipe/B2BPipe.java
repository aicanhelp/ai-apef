package io.apef.core.channel.pipe;

import io.apef.core.channel.BusinessChannel;

import io.apef.core.channel.request.B2BRequest;

public interface B2BPipe<S extends BusinessChannel<S>, D extends BusinessChannel<D>>
        extends ChannelTxPipe<S, D>, B2BNoReplyPipe<D> {
    B2BRequest<? extends B2BRequest, Object, Object> request();
}
