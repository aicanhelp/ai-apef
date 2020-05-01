package io.apef.connector.base;

import io.apef.core.channel.MessageType;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConnectorChannelContextManager<T extends ConnectorChannelContext> {
    private String name;
    @Getter
    @Accessors(fluent = true)
    private T defaultContext;
    private T[] channelContexts;
    private ChannelContextArrayCreator<T> channelContextArrayCreator;
    @Getter
    @Accessors(fluent = true)
    private boolean byPass;

    public interface ChannelContextArrayCreator<T> {
        T[] create(int size);
    }

    public ConnectorChannelContextManager(String name, ChannelContextArrayCreator<T> channelContextArrayCreator) {
        this.name = name;
        this.channelContextArrayCreator = channelContextArrayCreator;
    }

    private void setByPassChannelContext(T channelContext) {
        if (this.defaultContext != null) {
            throw new IllegalArgumentException(name + " failed to set ChannelContext with byPass: " +
                    "another ChannelContext was set already");
        }
        if (this.channelContexts != null) {
            throw new IllegalArgumentException(name + " failed to set ChannelContext with byPass: " +
                    "another ChannelContexts without byPass were set already");
        }
        this.defaultContext = channelContext;
        this.byPass = true;
    }

    private void setNoTypeChannelContext(T channelContext) {
        if (this.defaultContext != null) {
            throw new IllegalArgumentException(name + " failed to set ChannelContext for NoType: " +
                    "another ChannelContext was set already");
        }
        this.defaultContext = channelContext;
    }

    private void setTypeChannelContext(T channelContext) {
        if (this.defaultContext != null && this.defaultContext.requestType().isByPass()) {
            throw new IllegalArgumentException(name + " failed to set ChannelContext: " +
                    "another ByPass ChannelContext was set already");
        }

        if (this.channelContexts == null) {
            this.channelContexts = this.channelContextArrayCreator.create(channelContext.requestType().id() + 1);
        }
        if (channelContext.requestType().id() < this.channelContexts.length) {
            this.channelContexts[channelContext.requestType().id()] = channelContext;
            return;
        }
        T[] newChannelContexts = this.channelContextArrayCreator.create(channelContext.requestType().id() + 1);
        for (T context : this.channelContexts) {
            newChannelContexts[context.requestType().id()] = context;
        }
        newChannelContexts[channelContext.requestType().id()] = channelContext;
        this.channelContexts = newChannelContexts;
    }

    public void registerChannelContext(T channelContext) {
        if (log.isDebugEnabled()) {
            log.debug("Register ConnectorChannelContext for requestType: " + channelContext.requestType());
        }
        if (channelContext.requestType().isByPass()) {
            this.setByPassChannelContext(channelContext);
            return;
        }

        if (channelContext.requestType().isNoType()) {
            this.setNoTypeChannelContext(channelContext);
            return;
        }

        this.setTypeChannelContext(channelContext);
    }

    public T channelContext(Byte requestTypeId) {
        if (requestTypeId == null) return this.defaultContext;
        if (MessageType.isNoType(requestTypeId)) return this.defaultContext;
        if (MessageType.isByPass(requestTypeId)) return this.defaultContext;
        if (this.channelContexts == null) return null;

        if (requestTypeId >= this.channelContexts.length) {
            return null;
        }
        return this.channelContexts[requestTypeId];
    }
}
