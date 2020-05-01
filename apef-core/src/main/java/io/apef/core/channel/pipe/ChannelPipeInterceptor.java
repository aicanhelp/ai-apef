package io.apef.core.channel.pipe;

import io.apef.core.channel.message.ChannelInternalRequestMessage;

/**
 * ChannelPipe interceptor invoked in Business Thread Context
 *
 * @param <I>
 */
public interface ChannelPipeInterceptor<I extends ChannelPipeInterceptor<I>> {
    interface InterceptorHandler<T, R, M extends ChannelInternalRequestMessage<T, R>> {
        void handle(M requestMessage);
    }
}
