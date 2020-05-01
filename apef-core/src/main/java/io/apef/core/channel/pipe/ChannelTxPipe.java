package io.apef.core.channel.pipe;

import io.apef.core.channel.Channel;
import io.apef.core.channel.request.ChannelTxRequest;

public interface ChannelTxPipe<S extends Channel, D extends Channel> extends ChannelNoReplyPipe<D> {

    /**
     * Source Channel
     *
     * @return
     */
    S srcChannel();

    ChannelTxRequest<? extends ChannelTxRequest, Object, Object> request();
}
