package io.apef.core.channel.impl;

import io.apef.core.channel.Channel;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class FromBNoReplyPipeImpl extends NoReplyChannelPipeImpl {
    private FromBChannelPipeSender sender;
    private FromBChannelPipeAcceptor acceptor;

    public FromBNoReplyPipeImpl(Channel destChannel) {
        super(null,
                destChannel);
        this.sender = new FromBChannelPipeSender(destChannel);
        this.acceptor = new FromBChannelPipeAcceptor(null);
    }
}
