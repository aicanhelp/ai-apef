package io.apef.core.channel.executor.streaming;


import io.apef.core.channel.future.ChannelFuture;

/**
 * Here define a ChannelStream to finish
 *
 * @param <O>
 */
public interface ChannelStream<O> {
    <O2> ChannelStream<O2> map(SuccessMapper<O, O2> successMapper);

    <O2> ChannelStream<O2> map(SuccessMapper<O, O2> successMapper, FailureMapper<O2> failureMapper);

    ChannelFuture<O> future();

    interface SuccessMapper<I, O> {
        ChannelFuture<O> map(I value);
    }

    interface FailureMapper<O> {
        ChannelFuture<O> map(String errMsg, Throwable ex);
    }
}
