package io.apef.core.channel.request;

public interface S2BNoReplyRequest<M extends S2BNoReplyRequest<M, T>, T>
        extends ChannelNoReplyRequest<M, T> {
}
