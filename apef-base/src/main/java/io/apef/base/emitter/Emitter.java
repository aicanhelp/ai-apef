package io.apef.base.emitter;

import io.netty.buffer.ByteBuf;

public interface Emitter {
    Emitter append(String value);

    Emitter append(byte[] value);

    Emitter append(byte value);

    Emitter append(Object value);

    Emitter append(ByteBuf byteBuf);

    Emitter append(byte[] bytes, int offset, int len);

    int length();
}
