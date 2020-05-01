package io.apef.core.channel.feature;

import io.apef.core.channel.message.ChannelInternalRequestMessage;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class ChannelMessageFeature {
    private ChannelInternalRequestMessage channelMessage;

    protected ChannelMessageFeature(ChannelInternalRequestMessage channelMessage) {
        this.channelMessage = channelMessage;
    }
}
