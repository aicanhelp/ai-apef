package io.apef.core;

import io.apef.core.channel.*;

public class APEF {
    public static BusinessChannel<?> createBusinessChannel(ChannelConfig channelConfig) {
        return new BusinessChannelImpl<>(channelConfig);
    }

    public static ServerChannel<?> createServerChannel(ChannelConfig channelConfig) {
        return new ServerChannelImpl<>(channelConfig);
    }

    public static ClientChannel<?> createClientChannel(ChannelConfig channelConfig) {
        return new ClientChannelImpl<>(channelConfig);
    }
}
