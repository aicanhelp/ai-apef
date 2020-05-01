package io.apef.core.channel.impl;

import io.apef.core.channel.Channel;
import io.apef.core.channel.pipe.B2BNoReplyPipe;
import io.apef.core.channel.pipe.B2CNoReplyPipe;
import io.apef.core.channel.pipe.S2BNoReplyPipe;

public abstract class NoReplyChannelPipeImpl extends AbstractChannelPipeImpl implements
        B2BNoReplyPipe, B2CNoReplyPipe, S2BNoReplyPipe {
    public NoReplyChannelPipeImpl(Channel srcChannel, Channel destChannel) {
        super(srcChannel, destChannel);
    }

    @Override
    public boolean hasReply() {
        return false;
    }
}
