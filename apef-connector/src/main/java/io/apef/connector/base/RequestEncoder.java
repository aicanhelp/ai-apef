package io.apef.connector.base;

import io.netty.buffer.ByteBuf;

/**
 * @param <OC> OutStream Context
 * @param <T>  Request
 */
public interface RequestEncoder<OC, T> {
    void encode(OC outContext, ByteBuf out, T requestData) throws Exception;
}
