package io.apef.core.channel.impl;

import io.apef.core.channel.BusinessChannel;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class ToBNoReplyPipeImpl extends NoReplyChannelPipeImpl {
    private ToBChannelPipeSender sender;
    private ToBChannelPipeAcceptor acceptor;

    public ToBNoReplyPipeImpl(BusinessChannel destChannel) {
        super(null, destChannel);
        this.sender = new ToBChannelPipeSender(destChannel);
        this.acceptor = new ToBChannelPipeAcceptor(null);
    }
}
