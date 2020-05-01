package io.apef.core.channel.request;

public interface B2CNoReplyRequest<M extends B2CNoReplyRequest<M, T>, T>
        extends ChannelNoReplyRequest<M, T> {
}
