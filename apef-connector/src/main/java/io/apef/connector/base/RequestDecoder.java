package io.apef.connector.base;

import io.netty.buffer.ByteBuf;

/**
 * @param <IC> inStream Context
 * @param <T>  Request
 */
public interface RequestDecoder<IC, T> {
    T decode(IC context, ByteBuf data) throws Exception;
}
