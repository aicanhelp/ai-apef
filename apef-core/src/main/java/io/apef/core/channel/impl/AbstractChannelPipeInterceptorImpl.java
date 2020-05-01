package io.apef.core.channel.impl;

import io.apef.core.channel.MessageType;
import io.apef.core.channel.message.ChannelInternalRequestMessage;
import io.apef.core.channel.message.MessageStatus;
import io.apef.core.channel.pipe.ChannelPipeInterceptor;


public abstract class AbstractChannelPipeInterceptorImpl<I extends AbstractChannelPipeInterceptorImpl<I>>
        implements ChannelPipeInterceptor<I> {

    static class PhasedInterceptor {
        private String name;

        private final InterceptorHandler[] messageHandlers = new InterceptorHandler[MessageType.MAX_USER_ID + 1];

        PhasedInterceptor(String name) {
            this.name = name;
        }

        public <T, R, M extends ChannelInternalRequestMessage<T, R>> void addHandler(MessageType messageType,
                                                                                     InterceptorHandler<T, R, M> handler) {
            synchronized (messageHandlers) {
                messageHandlers[messageType.id()] = handler;
            }
        }

        public <T, R> void handle(ChannelInternalRequestMessage<T, R> message) {
            if (message.status() != MessageStatus.Finished) {
                InterceptorHandler handler = messageHandlers[message.messageType().id()];
                if (handler != null) {
                    handler.handle(message);
                }
            }
        }
    }
}
