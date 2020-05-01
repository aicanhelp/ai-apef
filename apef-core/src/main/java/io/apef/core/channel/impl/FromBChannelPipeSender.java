package io.apef.core.channel.impl;

import io.apef.core.channel.Channel;
import io.apef.core.channel.feature.idem.IdempotentHandler;
import io.apef.core.channel.message.ChannelInternalRequestMessage;
import io.apef.core.channel.message.MessageStatus;
import io.apef.core.channel.pipe.FromBInterceptor;
import io.apef.core.channel.pipe.ChannelPipeSender;

import java.util.concurrent.TimeUnit;

public class FromBChannelPipeSender implements ChannelPipeSender {
    private Channel<?> destChannel;
    private FromBInterceptor<?> interceptor;

    private IdempotentHandler idempotentHandler = new IdempotentHandler();

    public FromBChannelPipeSender(Channel<?> destChannel) {
        this.destChannel = destChannel;
    }

    public FromBChannelPipeSender(Channel<?> destChannel,
                                  FromBInterceptor<?> interceptor) {
        this.destChannel = destChannel;
        this.interceptor = interceptor;
    }

    /**
     * For test only
     *
     * @param interceptor
     */
    public void setInterceptor(FromBInterceptor<?> interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public <T, R> void send(ChannelInternalRequestMessage<T, R> message) {
        if (this.interceptor != null)
            this.interceptor.beforeSendRequest(message);

        //The interceptor may change the status of message, so here needs to check the status
        //Because this method is invoked by the Server ThreadContext,
        //So, here should not have the intercept operation
        if (message.status() == MessageStatus.Sent) {
            this.idempotentHandler.handle(message);
            if (message.features().timeout() != null) {
                message.features().timeout().start();
            }
            //Only sent the primary message
            if (message.features().primary())
                this.destChannel.write(message);
        }
    }

    @Override
    public <T, R> void retry(ChannelInternalRequestMessage<T, R> message, int delayMs) {
        message.channelPipe()
                .srcChannel()
                .schedule(() -> {
                            if (interceptor != null)
                                interceptor.beforeSendRequest(message);
                            message.retry();
                        },
                        delayMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public <T, R> void handleResponse(ChannelInternalRequestMessage<T, R> message) {

        if (!doRetry(message)) {

            this.idempotentHandler.end(message);
            //!!!keep the following lines order
            if (message.features().idem() != null) {
                message.features().idem().finishFollowers();
            }

            if (this.interceptor != null)
                this.interceptor.beforeHandleResponse(message);

            message.finish();
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
}
