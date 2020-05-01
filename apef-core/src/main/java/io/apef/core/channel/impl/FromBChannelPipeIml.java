package io.apef.core.channel.impl;

import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.Channel;
import io.apef.core.channel.pipe.FromBInterceptor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class FromBChannelPipeIml extends AbstractChannelPipeImpl {
    private FromBChannelPipeSender sender;
    private FromBChannelPipeAcceptor acceptor;

    public FromBChannelPipeIml(BusinessChannel srcChannel, Channel destChannel) {
        super(srcChannel, destChannel);
        this.sender = new FromBChannelPipeSender(destChannel);
        this.acceptor = new FromBChannelPipeAcceptor(srcChannel);
    }

    public FromBChannelPipeIml(BusinessChannel srcChannel, Channel destChannel,
                               FromBInterceptor fromBInterceptor) {
        super(srcChannel, destChannel);
        this.sender = new FromBChannelPipeSender(destChannel, fromBInterceptor);
        this.acceptor = new FromBChannelPipeAcceptor(srcChannel);
    }
}
