package io.apef.base.utils;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class ByteBufUtils {
    public static ByteBuf heapPooled(ByteBuf byteBuf) {
        return PooledByteBufAllocator.DEFAULT.heapBuffer(byteBuf.readableBytes())
                .writeBytes(byteBuf);
    }

    public static ByteBuf directPooled(ByteBuf byteBuf) {
        return PooledByteBufAllocator.DEFAULT.directBuffer(byteBuf.readableBytes())
                .writeBytes(byteBuf);
    }
}
