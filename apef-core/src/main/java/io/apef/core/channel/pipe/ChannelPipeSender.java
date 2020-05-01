package io.apef.core.channel.pipe;

import io.apef.core.channel.message.ChannelInternalRequestMessage;

/**
 * A ChannelPipe should have a message sender:
 * (1) according to the status of message to decide whether or not send the message out
 * (2) do retry to re-send the message
 * (3) according to the status of message to decide whether or not handle the response
 */
public interface ChannelPipeSender {
    /**
     * Send the request message
     * (1) For S2B ChannelPipe, it'is invoked in Server ThreadContext
     * (2) For B2C ChannelPipe, it's invoked in Business ThreadContext
     *
     * @param message
     * @param <T>
     * @param <R>
     */
    <T, R> void send(ChannelInternalRequestMessage<T, R> message);

    /**
     * do retry to re-send the message
     * (1) For S2B ChannelPipe, it'is invoked in Timer ThreadContext
     * (2) For B2C ChannelPipe, it's invoked in Business ThreadContext as well
     *
     * @param message
     * @param <T>
     * @param <R>
     */
    <T, R> void retry(ChannelInternalRequestMessage<T, R> message,int delayMs);

    /**
     * After messageBox accept the response, message box let the sender to handle the response
     * (1) For S2B ChannelPipe, it'is invoked in Business ThreadContext
     * (2) For B2C ChannelPipe, it's invoked in Business ThreadContext as well
     * @param message
     * @param <T>
     * @param <R>
     */
    <T, R> void handleResponse(ChannelInternalRequestMessage<T, R> message);
}
