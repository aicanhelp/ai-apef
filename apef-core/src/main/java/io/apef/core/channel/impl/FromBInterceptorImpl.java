package io.apef.core.channel.impl;

import io.apef.core.channel.MessageType;
import io.apef.core.channel.message.ChannelInternalRequestMessage;
import io.apef.core.channel.pipe.ChannelPipeInterceptor;
import io.apef.core.channel.pipe.FromBInterceptor;

public class FromBInterceptorImpl extends AbstractChannelPipeInterceptorImpl<FromBInterceptorImpl>
        implements FromBInterceptor<FromBInterceptorImpl> {
    private final AbstractChannelPipeInterceptorImpl.PhasedInterceptor beforeSendRequestInterceptor = new AbstractChannelPipeInterceptorImpl.PhasedInterceptor("beforeSendRequest");
    private final AbstractChannelPipeInterceptorImpl.PhasedInterceptor beforeHandleResponseInterceptor = new AbstractChannelPipeInterceptorImpl.PhasedInterceptor("beforeHandleResponse");

    public <T, R, M extends ChannelInternalRequestMessage<T, R>> FromBInterceptorImpl beforeSendRequest(MessageType messageType,
                                                                                                        ChannelPipeInterceptor.InterceptorHandler<T, R, M> handler) {
        this.beforeSendRequestInterceptor.addHandler(messageType, handler);
        return this;
    }

    public <T, R, M extends ChannelInternalRequestMessage<T, R>> FromBInterceptorImpl beforeHandleResponse(MessageType messageType,
                                                                                                           ChannelPipeInterceptor.InterceptorHandler<T, R, M> handler) {
        this.beforeHandleResponseInterceptor.addHandler(messageType, handler);
        return this;
    }

    public <T, R> void beforeSendRequest(ChannelInternalRequestMessage<T, R> requestMessage) {
        this.beforeSendRequestInterceptor.handle(requestMessage);
    }

    public <T, R> void beforeHandleResponse(ChannelInternalRequestMessage<T, R> responseMessage) {
        this.beforeHandleResponseInterceptor.handle(responseMessage);
    }
}
