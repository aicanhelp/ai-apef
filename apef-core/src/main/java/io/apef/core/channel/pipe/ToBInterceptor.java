package io.apef.core.channel.pipe;

import io.apef.core.channel.MessageType;
import io.apef.core.channel.message.ChannelInternalRequestMessage;

public interface ToBInterceptor<I extends ChannelPipeInterceptor<I>>
        extends ChannelPipeInterceptor<I> {

    /**
     * invoked in Business ThreadContext
     *
     * @param requestMessage
     * @param <T>
     * @param <R>
     */
    <T, R> void beforeHandleRequest(ChannelInternalRequestMessage<T, R> requestMessage);

    /**
     * invoked in Business ThreadContext
     *
     * @param requestMessage
     * @param <T>
     * @param <R>
     */
    <T, R> void beforeSendResponse(ChannelInternalRequestMessage<T, R> requestMessage);

    /**
     * Set handler for beforeHandleRequest
     *
     * @param messageType
     * @param handler
     * @param <T>
     * @param <R>
     * @param <M>
     * @return
     */
    <T, R, M extends ChannelInternalRequestMessage<T, R>> I beforeHandleRequest(MessageType messageType, InterceptorHandler<T, R, M> handler);

    /**
     * Set handler for beforeSendResponse
     *
     * @param messageType
     * @param handler
     * @param <T>
     * @param <R>
     * @param <M>
     * @return
     */
    <T, R, M extends ChannelInternalRequestMessage<T, R>> I beforeSendResponse(MessageType messageType, InterceptorHandler<T, R, M> handler);

}

