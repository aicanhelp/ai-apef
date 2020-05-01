package io.apef.sdef.connector.tcp;

import io.netty.buffer.ByteBuf;

public interface ITcpContext {
    void response(ByteBuf data);

    ByteBuf request();
}
