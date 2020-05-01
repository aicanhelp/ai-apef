package io.apef.core.channel.pipe;

import io.apef.core.channel.ChannelHandler;
import io.apef.core.channel.message.ChannelInternalRequestMessage;

/**
 * A ChannelPipe should have a acceptor to:
 * (1) handle the request
 * (2) send the response
 */
public interface ChannelPipeAcceptor {
    /**
     * Send response
     * (1) For S2B ChannelPipe, it is invoked in Business Thread
     * (2) For B2C ChannelPipe, it is invoked in Client Thread
     *
     * @param message
     * @param <T>
     * @param <R>
     */
    <T, R> void response(ChannelInternalRequestMessage<T, R> message);

    /**
     * After messageBox accept the message, messageBox let the Acceptor to handle the accepted message
     * (1) For S2B ChannelPipe, the handler is invoked in Business Thread
     * (2) For B2C ChannelPipe, the handler is invoked in Business Thread as well
     *
     * @param message
     * @param channelHandler
     * @param <T>
     * @param <R>
     */
    <T, R> void accept(ChannelInternalRequestMessage<T, R> message,
                       ChannelHandler<T, R, ChannelInternalRequestMessage<T, R>> channelHandler);
}
