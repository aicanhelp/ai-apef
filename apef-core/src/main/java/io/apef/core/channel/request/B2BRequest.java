package io.apef.core.channel.request;

public interface B2BRequest<M extends B2BRequest<M, T, R>, T, R>
        extends ChannelTxRequest<M, T, R>, B2BNoReplyRequest<M, T> {

}

