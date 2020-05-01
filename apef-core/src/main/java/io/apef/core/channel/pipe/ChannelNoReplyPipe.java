package io.apef.core.channel.pipe;

import io.apef.core.channel.Channel;
import io.apef.core.channel.request.ChannelNoReplyRequest;

public interface ChannelNoReplyPipe<D extends Channel> extends ChannelPipe {
    /**
     * DestinationChannel
     *
     * @return
     */
    D destChannel();

    /**
     * Create ChannelNoReplyRequest for this pipe without messageType
     *
     * @return
     */
    ChannelNoReplyRequest<? extends ChannelNoReplyRequest, Object> noReply();
}
