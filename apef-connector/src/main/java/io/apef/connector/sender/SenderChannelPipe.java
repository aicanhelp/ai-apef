package io.apef.connector.sender;


import io.apef.core.channel.MessageType;
import io.apef.core.channel.pipe.B2CPipe;

public interface SenderChannelPipe extends B2CPipe {
    <M extends SenderRequest<M, T, R>, T, R> M send(MessageType requestType);
    <M extends SenderRequest<M, T, R>, T, R> M send();
}
