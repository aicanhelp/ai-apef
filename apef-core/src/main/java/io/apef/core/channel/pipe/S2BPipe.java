package io.apef.core.channel.pipe;

import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.ServerChannel;
import io.apef.core.channel.request.S2BRequest;

public interface S2BPipe<S extends ServerChannel, D extends BusinessChannel<D>>
        extends ChannelTxPipe<S, D>, S2BNoReplyPipe<D> {
    S2BRequest<? extends S2BRequest, Object, Object> request();
}
