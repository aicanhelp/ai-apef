package io.apef.core.channel.impl;


import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.Channel;
import io.apef.core.channel.pipe.ToBInterceptor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class ToBChannelPipeIml extends AbstractChannelPipeImpl {
    private ToBChannelPipeSender sender;
    private ToBChannelPipeAcceptor acceptor;

    public ToBChannelPipeIml(Channel srcChannel,
                             BusinessChannel destChannel) {
        super(srcChannel, destChannel);
        this.sender = new ToBChannelPipeSender(destChannel);
        this.acceptor = new ToBChannelPipeAcceptor(srcChannel);
    }

    public ToBChannelPipeIml(Channel srcChannel,
                             BusinessChannel destChannel, ToBInterceptor toBInterceptor) {
        super(srcChannel, destChannel);
        this.sender = new ToBChannelPipeSender(destChannel);
        this.acceptor = new ToBChannelPipeAcceptor(srcChannel, toBInterceptor);
    }
}

