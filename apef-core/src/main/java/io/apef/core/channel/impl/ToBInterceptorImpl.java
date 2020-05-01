package io.apef.core.channel.impl;

import io.apef.core.channel.MessageType;
import io.apef.core.channel.message.ChannelInternalRequestMessage;
import io.apef.core.channel.pipe.ToBInterceptor;

public class ToBInterceptorImpl extends AbstractChannelPipeInterceptorImpl<ToBInterceptorImpl>
        implements ToBInterceptor<ToBInterceptorImpl> {

    private final PhasedInterceptor beforeHandleRequestInterceptor = new PhasedInterceptor("beforeHandleRequest");
    private final PhasedInterceptor beforeSendResponseInterceptor = new PhasedInterceptor("beforeSendResponse");

    public <T, R, M extends ChannelInternalRequestMessage<T, R>> ToBInterceptorImpl beforeHandleRequest(MessageType messageType,
                                                                                                        InterceptorHandler<T, R, M> handler) {
        this.beforeHandleRequestInterceptor.addHandler(messageType, handler);
        return this;
    }

    public <T, R, M extends ChannelInternalRequestMessage<T, R>> ToBInterceptorImpl beforeSendResponse(MessageType messageType,
                                                                                                       InterceptorHandler<T, R, M> handler) {
        this.beforeSendResponseInterceptor.addHandler(messageType, handler);
        return this;
    }

    public <T, R> void beforeHandleRequest(ChannelInternalRequestMessage<T, R> requestMessage) {
        this.beforeHandleRequestInterceptor.handle(requestMessage);
    }

    public <T, R> void beforeSendResponse(ChannelInternalRequestMessage<T, R> responseMessage) {
        this.beforeSendResponseInterceptor.handle(responseMessage);
    }

}
