package io.apef.connector.base;


import io.netty.buffer.ByteBuf;

/**
 * @param <IC> inStream context
 * @param <T>  request data
 * @param <R>  response data
 */
public interface ResponseDecoder<IC, T, R> {
    R decode(IC inContext, T requestData, ByteBuf response) throws Exception;
}
