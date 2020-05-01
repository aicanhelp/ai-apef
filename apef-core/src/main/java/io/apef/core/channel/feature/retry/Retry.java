package io.apef.core.channel.feature.retry;

import io.apef.core.channel.feature.ChannelMessageFeature;
import io.apef.core.channel.message.ChannelInternalRequestMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Retry extends ChannelMessageFeature {

    private int retryCount = 0;
    private int intervalMs = -1;

    public Retry(int retryCount, int intervalMs, ChannelInternalRequestMessage channelMessage) {
        super(channelMessage);
        this.retryCount = retryCount;
        this.intervalMs = intervalMs;
    }

    public boolean retry() {
        if (retryCount <= 0) return false;
        retryCount = retryCount - 1;
        channelMessage().channelPipe()
                .sender().retry(channelMessage(), intervalMs);

        return true;
    }
}
