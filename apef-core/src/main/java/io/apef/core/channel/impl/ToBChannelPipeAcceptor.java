package io.apef.core.channel.impl;

import io.apef.core.channel.Channel;
import io.apef.core.channel.ChannelHandler;
import io.apef.core.channel.feature.idem.IdempotentHandler;
import io.apef.core.channel.message.ChannelInternalRequestMessage;
import io.apef.core.channel.message.MessageStatus;
import io.apef.core.channel.pipe.ChannelPipeAcceptor;
import io.apef.core.channel.pipe.ToBInterceptor;
import io.apef.core.channel.request.DefaultMessageType;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ToBChannelPipeAcceptor implements ChannelPipeAcceptor {
    private Channel<?> srcChannel;
    private ToBInterceptor<?> interceptor;
    private IdempotentHandler idempotentHandler = new IdempotentHandler();

    public ToBChannelPipeAcceptor(Channel<?> srcChannel) {
        this.srcChannel = srcChannel;
    }

    public ToBChannelPipeAcceptor(Channel<?> srcChannel,
                                  ToBInterceptor<?> interceptor) {
        this.srcChannel = srcChannel;
        this.interceptor = interceptor;
    }

    /**
     * For test only
     *
     * @param interceptor
     */
    public void setInterceptor(ToBInterceptor<?> interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public <T, R> void response(ChannelInternalRequestMessage<T, R> message) {
        if (!doRetry(message)) {
            this.idempotentHandler.end(message);

            if (this.interceptor != null) {
                this.interceptor.beforeSendResponse(message);
            }
            //whatever any status, send the response
            this.srcChannel.write(DefaultMessageType.Response, message);
        }
    }

    //invoke on business Channel thread
    private boolean doRetry(ChannelInternalRequestMessage requestMessage) {
        if (requestMessage.response().success()) return false;
        if (requestMessage.features().timeout() != null
                && requestMessage.features().timeout().isDone()) {
            return false;
        }
        return requestMessage.features().retry() != null &&
                requestMessage.features().retry().retry();
    }

    @Override
    public <T, R> void accept(ChannelInternalRequestMessage<T, R> message,
                              ChannelHandler<T, R, ChannelInternalRequestMessage<T, R>> channelHandler) {
        if (this.interceptor != null)
            this.interceptor.beforeHandleRequest(message);

        //the interceptor may change the status of message
        if (message.status() != MessageStatus.Finished) {
            this.idempotentHandler.handle(message);

            if (message.features().timeout() != null) {
                message.features().timeout().start();
            }

            //Only handle the primary message
            if (message.features().primary()) {
                channelHandler.handle(message);
            }
        }
    }

}
