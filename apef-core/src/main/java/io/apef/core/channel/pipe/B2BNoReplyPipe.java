package io.apef.core.channel.pipe;

import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.request.B2BNoReplyRequest;

public interface B2BNoReplyPipe<D extends BusinessChannel<D>> extends ChannelNoReplyPipe<D> {
    B2BNoReplyRequest<? extends B2BNoReplyRequest, Object> noReply();
}
