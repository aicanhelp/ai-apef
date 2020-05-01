package io.apef.core.channel;

import io.apef.core.channel.box.DirectMessageBox;

public class ServerChannelImpl<C extends ServerChannelImpl<C>>
        extends AbstractChannel<C> implements ServerChannel<C> {
    public ServerChannelImpl(ChannelConfig channelConfig) {
        super(channelConfig, new DirectMessageBox(channelConfig.getName()));
    }
}
