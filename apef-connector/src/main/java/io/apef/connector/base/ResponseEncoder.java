package io.apef.connector.base;

import io.netty.buffer.ByteBuf;

/**
 * @param <OC> inStream Context
 * @param <R>  Response
 */
public interface ResponseEncoder<OC, R> {
    void encode(OC outContext, ByteBuf out, R response) throws Exception;
}
