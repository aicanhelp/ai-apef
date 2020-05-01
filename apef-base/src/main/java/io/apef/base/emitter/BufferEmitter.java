package io.apef.base.emitter;

import io.apef.base.utils.Bytes;
import io.netty.buffer.ByteBuf;
import io.vertx.core.buffer.Buffer;

public class BufferEmitter implements Emitter {
    protected Buffer buffer;

    public BufferEmitter() {
        this((Buffer) null);
    }

    public BufferEmitter(Buffer buffer) {
        this.buffer = buffer;
        if (this.buffer == null)
            this.buffer = Buffer.buffer(8096);
    }

    public BufferEmitter(ByteBuf byteBuf) {
        this.buffer = Buffer.buffer(byteBuf);
    }

    public BufferEmitter(int initSize) {
        buffer = Buffer.buffer(initSize);
    }

    public Buffer buffer() {
        return buffer;
    }

    public int length() {
        return this.buffer.length();
    }

    @Override
    public Emitter append(String value) {
        if (value == null) return this;
        buffer.appendBytes(Bytes.bytesOf(value));
        return this;
    }

    @Override
    public Emitter append(byte[] value) {
        if (value == null) return this;
        buffer.appendBytes(value);
        return this;
    }

    @Override
    public Emitter append(byte value) {
        buffer.appendByte(value);
        return this;
    }

    @Override
    public Emitter append(Object value) {
        if (value == null) return this;
        buffer.appendString(String.valueOf(value));
        return this;
    }

    @Override
    public Emitter append(ByteBuf byteBuf) {
        if (byteBuf != null) {
            this.buffer.appendBuffer(Buffer.buffer(byteBuf));
        }
        return this;
    }

    public Emitter append(Buffer buffer) {
        if (buffer != null) {
            this.buffer.appendBuffer(buffer);
        }
        return this;
    }

    public Emitter append(byte[] bytes, int offset, int len) {
        if (bytes != null) {
            this.buffer.appendBytes(bytes, offset, len);
        }
        return this;
    }

    public String toString() {
        return this.buffer.toString();
    }


}
