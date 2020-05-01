package io.apef.core.channel.pipe;

import io.apef.core.channel.MessageType;
import io.apef.core.channel.message.ChannelInternalRequestMessage;

/**
 * ChannelPipe interceptor invoked in Business Thread Context
 *
 * @param <I>
 */
public interface FromBInterceptor<I extends ChannelPipeInterceptor<I>>
        extends ChannelPipeInterceptor<I> {
    /**
     * invoked in Business ThreadContext
     *
     * @param requestMessage
     * @param <T>
     * @param <R>
     */
    <T, R> void beforeSendRequest(ChannelInternalRequestMessage<T, R> requestMessage);

    /**
     * invoked in Business ThreadContext
     *
     * @param responseMessage
     * @param <T>
     * @param <R>
     */
    <T, R> void beforeHandleResponse(ChannelInternalRequestMessage<T, R> responseMessage);

    /**
     * Set handler for beforeSendRequest
     *
     * @param messageType
     * @param handler
     * @param <T>
     * @param <R>
     * @param <M>
     * @return
     */
    <T, R, M extends ChannelInternalRequestMessage<T, R>> I beforeSendRequest(MessageType messageType,
                                                                              InterceptorHandler<T, R, M> handler);

    /**
     * Set handler for beforeHandleResponse
     *
     * @param messageType
     * @param handler
     * @param <T>
     * @param <R>
     * @param <M>
     * @return
     */
    <T, R, M extends ChannelInternalRequestMessage<T, R>> I beforeHandleResponse(MessageType messageType, InterceptorHandler<T, R, M> handler);

}
