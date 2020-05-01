package io.apef.base.emitter;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.vertx.core.buffer.Buffer;

//unusable completely, don't use it. need more best practice
public class DirectBufEmitter extends BufferEmitter {
    private ByteBuf byteBuf;

    public DirectBufEmitter() {
        this(8096);
    }

    public DirectBufEmitter(int size) {
        this.byteBuf = PooledByteBufAllocator.DEFAULT.buffer(size);
        this.buffer = Buffer.buffer(this.byteBuf);
    }

    public void release() {
        this.byteBuf.release();
    }
}
