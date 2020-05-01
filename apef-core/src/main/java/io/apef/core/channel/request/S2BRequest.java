package io.apef.core.channel.request;

public interface S2BRequest<M extends S2BRequest<M, T, R>, T, R>
        extends ChannelTxRequest<M, T, R>, S2BNoReplyRequest<M, T> {

}