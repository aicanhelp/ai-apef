package io.apef.core.channel;

import io.apef.core.channel.box.DisruptorMessageBox;
import io.apef.core.channel.impl.ToBChannelPipeIml;
import io.apef.core.channel.impl.ToBNoReplyPipeImpl;
import io.apef.core.channel.pipe.*;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * BusinessChannelImpl
 * Make all messages for this channel be handled in the same thread
 */
@Slf4j
public class BusinessChannelImpl<C extends BusinessChannelImpl<C>>
        extends AbstractChannel<C> implements BusinessChannel<C> {
    public BusinessChannelImpl(ChannelConfig channelConfig) {
        super(channelConfig,
                new DisruptorMessageBox(channelConfig.getName() + "_box",
                        channelConfig.getQueueSize(), 1,
                        new DefaultThreadFactory(channelConfig.getName())));
    }

    @Override
    public B2BNoReplyPipe<C> B2BNoReplyPipe() {
        return new ToBNoReplyPipeImpl(this);
    }

    @Override
    public B2BPipe<?, C> B2BPipe(BusinessChannel fromChannel) {
        return new ToBChannelPipeIml(fromChannel, this);
    }

    @Override
    public S2BNoReplyPipe<C> S2BNoReplyPipe() {
        return new ToBNoReplyPipeImpl(this);
    }

    @Override
    public S2BPipe<?, C> S2BPipe(ServerChannel fromChannel) {
        return new ToBChannelPipeIml(fromChannel, this);
    }
}
