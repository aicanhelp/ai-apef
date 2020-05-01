package io.apef.base.emitter;

import io.apef.base.utils.Bytes;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ByteBufEmitter implements Emitter {
    private ByteBuf buf;

    public ByteBufEmitter() {
        this(null);
    }

    public ByteBufEmitter(int initSize) {
        this.buf = Unpooled.buffer(initSize);
    }

    public ByteBufEmitter(ByteBuf buf) {
        this.buf = buf;
        if (buf == null) {
            this.buf = Unpooled.buffer();
        }
    }

    @Override
    public Emitter append(String value) {
        this.buf.writeBytes(Bytes.bytesOf(value));
        return this;
    }

    @Override
    public Emitter append(byte[] value) {
        this.buf.writeBytes(value);
        return this;
    }

    @Override
    public Emitter append(byte value) {
        this.buf.writeByte(value);
        return this;
    }

    @Override
    public Emitter append(Object value) {
        this.buf.writeBytes(Bytes.bytesOf(String.valueOf(value)));
        return this;
    }

    @Override
    public Emitter append(ByteBuf byteBuf) {
        this.buf.writeBytes(byteBuf);
        return this;
    }

    @Override
    public Emitter append(byte[] bytes, int offset, int len) {
        this.buf.writeBytes(bytes, offset, len);
        return this;
    }

    public ByteBuf byteBuf() {
        return this.buf;
    }

    @Override
    public int length() {
        return this.buf.readableBytes();
    }
}
