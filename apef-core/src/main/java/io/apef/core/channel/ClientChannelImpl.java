package io.apef.core.channel;

import io.apef.core.channel.box.DirectMessageBox;
import io.apef.core.channel.impl.FromBChannelPipeIml;
import io.apef.core.channel.impl.FromBNoReplyPipeImpl;
import io.apef.core.channel.pipe.B2CNoReplyPipe;
import io.apef.core.channel.pipe.B2CPipe;

public class ClientChannelImpl<C extends ClientChannelImpl<C>>
        extends AbstractChannel<C> implements ClientChannel<C> {
    public ClientChannelImpl(ChannelConfig channelConfig) {
        super(channelConfig, new DirectMessageBox(channelConfig.getName()));
    }

    @Override
    public B2CNoReplyPipe<C> B2CNoReplyPipe() {
        return new FromBNoReplyPipeImpl(this);
    }

    @Override
    public B2CPipe<?, C> B2CPipe(BusinessChannel fromChannel) {
        return new FromBChannelPipeIml(fromChannel, this);
    }
}