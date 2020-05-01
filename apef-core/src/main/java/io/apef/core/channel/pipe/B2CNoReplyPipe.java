package io.apef.core.channel.pipe;

import io.apef.core.channel.ClientChannel;
import io.apef.core.channel.request.B2CNoReplyRequest;

public interface B2CNoReplyPipe<D extends ClientChannel> extends ChannelNoReplyPipe<D> {
    B2CNoReplyRequest<? extends B2CNoReplyRequest, Object> noReply();
}