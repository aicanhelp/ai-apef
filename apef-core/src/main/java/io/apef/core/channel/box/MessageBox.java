package io.apef.core.channel.box;

import io.apef.core.channel.MessageType;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

public interface MessageBox {
    MessageBox put(MessageType messageType, Object message);

    MessageBoxId messageBoxId();

    <T> MessageBox handler(MessageType messageType, MessageBoxHandler<T> handler);

    MessageBox start();

    void close();

    @ToString
    class MessageEvent {
        MessageType messageType;
        Object message;
        int handled;

        public MessageEvent() {
        }

        public MessageEvent(MessageType messageType, Object message) {
            this.messageType = messageType;
            this.message = message;
        }

        void reset() {
            messageType = null;
            message = null;
        }

        void handled() {
            this.handled = this.handled + 1;
        }
    }

    interface MessageBoxHandler<T> {
        void handle(MessageType messageType, T message);
    }

    @Slf4j
    class HandlerChain {
        final MessageBoxHandler[] messageBoxHandlers = new MessageBoxHandler[MessageType.MAX_ID + 1];

        public HandlerChain(MessageType messageType, MessageBoxHandler handler) {
            this.addHandler(messageType, handler);
        }

        public boolean handleEvent(MessageEvent event) {
            if (log.isDebugEnabled()) {
                log.debug("Handling MessageEvent: " + event);
            }
            MessageBoxHandler messageBoxHandler = messageBoxHandlers[event.messageType.id()];
            if (messageBoxHandler == null) {
                return false;
            }

            messageBoxHandler.handle(event.messageType, event.message);
            return true;
        }

        public void addHandler(MessageType messageType, MessageBoxHandler handler) {
            this.messageBoxHandlers[messageType.id()] = handler;
        }
    }
}
