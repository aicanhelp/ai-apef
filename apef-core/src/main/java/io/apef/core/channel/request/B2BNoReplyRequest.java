package io.apef.core.channel.request;

public interface B2BNoReplyRequest<M extends B2BNoReplyRequest<M, T>, T>
        extends ChannelNoReplyRequest<M, T> {
}

