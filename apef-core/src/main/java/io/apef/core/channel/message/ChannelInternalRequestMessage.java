package io.apef.core.channel.message;

import io.apef.core.channel.feature.ChannelMessageFeatures;
import io.apef.core.channel.future.ChannelFuture;
import io.apef.core.channel.impl.ChannelInternalResponse;
import io.apef.core.channel.pipe.ChannelInternalPipe;

public interface ChannelInternalRequestMessage<T, R>
        extends ChannelMessage<T, R>, ChannelMessageContext<R> {

    ChannelInternalPipe channelPipe();

    ChannelMessageFeatures<T, R> features();

    void finish();

    /**
     * method for being called by the timeout timer
     */
    void timeout();

    void retry();

    /**
     * Before handling timeout message, change the status of message
     */
    void finishByTimeout();

    void finishByFollow(ChannelInternalRequestMessage<T, R> message);

    MessageStatus status();

    ChannelInternalRequestMessage<T, R> noReply();

    ChannelInternalResponse<T, R> response();

    ChannelFuture<R> responseFuture();
}
