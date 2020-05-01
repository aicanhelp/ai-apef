package io.apef.connector.base;

import io.apef.core.channel.MessageType;
import com.google.common.base.Preconditions;

public interface ConnectorChannelContext {
    MessageType requestType();

    default void check(ConnectorChannelContext context) {
        Preconditions.checkNotNull(context.requestType(),
                context.getClass().getSimpleName() + ":RequestType is required");
    }
}
