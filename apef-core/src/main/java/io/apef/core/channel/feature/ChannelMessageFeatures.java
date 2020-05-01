package io.apef.core.channel.feature;

import io.apef.core.channel.feature.idem.Idem;
import io.apef.core.channel.feature.retry.Retry;
import io.apef.core.channel.feature.timeout.Timeout;
import io.apef.core.channel.message.ChannelInternalRequestMessage;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class ChannelMessageFeatures<T, R> {
    private Idem idem;
    private Retry retry;
    private Timeout timeout;

    private ChannelInternalRequestMessage<T, R> requestMessage;

    public ChannelMessageFeatures(ChannelInternalRequestMessage<T, R> requestMessage) {
        this.requestMessage = requestMessage;
    }

    public ChannelMessageFeatures<T, R> idem(Object key) {
        this.idem = new Idem(key, this.requestMessage);
        return this;
    }

    public ChannelMessageFeatures<T, R> retry(int retryCount, int intervalMs) {
        if (retryCount > 0)
            this.retry = new Retry(retryCount, intervalMs, this.requestMessage);
        return this;
    }

    public ChannelMessageFeatures<T, R> timeout(long timeoutMs) {
        if (timeoutMs > 0)
            this.timeout = new Timeout(timeoutMs, this.requestMessage);
        return this;
    }

    public void cancelTimeout() {
        if (this.timeout == null) return;
        this.timeout.cancel();
    }

    public boolean primary() {
        return idem == null || idem.primary();
    }
}
