package io.apef.core.channel.impl;

import io.apef.core.channel.MessageType;
import io.apef.core.channel.feature.ChannelMessageFeatures;
import io.apef.core.channel.message.ChannelInternalRequestMessage;
import io.apef.core.channel.message.ChannelMessageContext;
import io.apef.core.channel.pipe.ChannelInternalPipe;
import io.apef.core.channel.pipe.ChannelPipeContext;
import io.apef.core.channel.request.ChannelInternalRequest;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class AbstractChannelMessageImpl<M extends AbstractChannelMessageImpl<M, T, R>, T, R>
        implements ChannelInternalRequest<M, T, R>,
        ChannelInternalRequestMessage<T, R> {
    private T requestContent;
    private MessageType messageType;
    private ChannelInternalPipe channelPipe;

    private ChannelMessageFeatures<T, R> features;

    protected AbstractChannelMessageImpl() {
        this.features = new ChannelMessageFeatures<>(this);
    }

    protected AbstractChannelMessageImpl(ChannelPipeContext channelPipeContext) {
        this();
        this.channelPipe = channelPipeContext.channelPipe();
    }

    public ChannelMessageContext<R> messageContext() {
        return this;
    }

    @Override
    public M messageType(MessageType messageType) {
        this.messageType = messageType;
        return (M) this;
    }

    @Override
    public M requestContent(T requestContent) {
        this.requestContent = requestContent;
        return (M) this;
    }

    @Override
    public M idem(Object key) {
        this.features.idem(key);
        return (M) this;
    }

    @Override
    public M timeout(int timeoutMS) {
        this.features.timeout(timeoutMS);
        return (M) this;
    }

    @Override
    public M retry(int retryCount, int intervalMs) {
        this.features.retry(retryCount, intervalMs);
        return (M) this;
    }

    @Override
    public String toString() {
        return "ChannelPipe: " + (this.channelPipe == null ? "" : this.channelPipe().toString()) + "; requestContent: " + requestContent;
    }
}
