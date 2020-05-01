package io.apef.core.channel.pipe;

import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.request.S2BNoReplyRequest;

public interface S2BNoReplyPipe<D extends BusinessChannel<D>> extends ChannelNoReplyPipe<D> {
    S2BNoReplyRequest<? extends S2BNoReplyRequest, Object> noReply();
}