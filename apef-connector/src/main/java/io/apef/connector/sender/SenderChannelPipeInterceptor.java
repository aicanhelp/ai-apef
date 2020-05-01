package io.apef.connector.sender;

import io.apef.core.channel.MessageType;
import io.apef.core.channel.impl.FromBInterceptorImpl;
import io.apef.connector.base.ConnectorChannelContextManager;

class SenderChannelPipeInterceptor<C extends ConnectorRequestContext<C>> extends FromBInterceptorImpl {
    private ConnectorChannelContextManager<SenderChannelContext>
            contextManager;

    SenderChannelPipeInterceptor(ConnectorChannelContextManager<SenderChannelContext> contextManager) {
        this.contextManager = contextManager;

        super.beforeSendRequest(SenderRequestMessage.TYPE, this::beforeRequest)
                .beforeHandleResponse(SenderRequestMessage.TYPE, this::afterRequest);
    }

    private void beforeRequest(SenderRequestContext<?, Object, Object, Object> message) {
        this.checkMessage(message);
        SenderChannelContext channelContext = this.contextManager.defaultContext();

        if (channelContext == null && message.requestType() != null) {
            channelContext = this.contextManager.channelContext(message.requestType().id());
        }

        if (channelContext == null) {
            message.fail("Failed to handle request: without Context for requestType: "
                    + message.requestType(), null);
            return;
        }

        message.attachSenderChannelContext(channelContext);
        this.handleBeforeRequest(message);
    }

    private void handleBeforeRequest(SenderRequestContext<?, Object, Object, Object> message) {
        if (message.ignoreCache()) return;

        Object result = message.channelContext().getFromCacheByRequest(message.requestContent());

        if (result != null) {
            message.hitCache(true);
            message.messageContext().succeed(result);
            message.finish();
        }
    }

    private void afterRequest(SenderRequestContext<C, Object, Object, Object> message) {
        if (message.ignoreCache() ||
                message.hitCache() ||
                message.response().responseContent() == null)
            return;

        message.channelContext().putResponseToCache(message.response().responseContent());
    }

    private void checkMessage(SenderRequestMessage message) {
        if (this.contextManager.byPass() && message.requestType() != null) {
            throw new IllegalArgumentException("ByPass Sender Channel does not support RequestType: " + message.requestType());
        }
        if (!this.contextManager.byPass() && message.requestType() == null) {
            message.requestType(MessageType.NO_TYPE);
        }
    }
}
