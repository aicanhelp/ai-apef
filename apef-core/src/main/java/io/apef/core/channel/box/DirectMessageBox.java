package io.apef.core.channel.box;

import io.apef.core.channel.MessageType;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DirectMessageBox implements MessageBox {
    @Getter
    @Accessors(fluent = true)
    private MessageBoxId messageBoxId;
    private HandlerChain handlerChain;

    public DirectMessageBox(String name) {
        this.messageBoxId = new MessageBoxId(name);
    }

    @Override
    public MessageBox put(MessageType messageType, Object message) {
        try {
            this.handlerChain.handleEvent(new MessageEvent(messageType, message));
        } catch (Exception ex) {
            log.error("Exception thrown in handling event:" + message, ex);
        }
        return this;
    }

    @Override
    public <T> MessageBox handler(MessageType messageType, MessageBoxHandler<T> handler) {
        if (handlerChain != null) {
            handlerChain.addHandler(messageType, handler);
            return this;
        }
        handlerChain = new HandlerChain(messageType, handler);
        return this;
    }

    @Override
    public DirectMessageBox start() {
        return this;
    }

    @Override
    public void close() {

    }
}
