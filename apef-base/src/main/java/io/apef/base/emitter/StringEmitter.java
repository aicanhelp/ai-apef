package io.apef.base.emitter;

import io.netty.buffer.ByteBuf;

import java.util.Arrays;

import static io.apef.base.utils.Bytes.*;

/**
 * Just for test, it is pool performance
 */
public class StringEmitter implements Emitter {
    StringBuilder stringBuilder;

    public StringEmitter() {
        this.stringBuilder = new StringBuilder(1024);
    }

    public StringEmitter(int initSize) {
        this.stringBuilder = new StringBuilder(initSize);
    }

    public Emitter append(String value) {
        if (value == null) return this;
        stringBuilder.append(value);
        return this;
    }

    public void reset() {
        this.stringBuilder.setLength(0);
    }

    public String toString() {
        return this.stringBuilder.toString();
    }

    @Override
    public Emitter append(byte[] value) {
        if (value == null) return this;
        stringBuilder.append(stringOf(value));
        return this;
    }

    @Override
    public Emitter append(byte value) {
        stringBuilder.append((char) value);
        return this;
    }

    @Override
    public Emitter append(Object value) {
        if (value == null) return this;
        stringBuilder.append(String.valueOf(value));
        return this;
    }

    @Override
    public Emitter append(ByteBuf byteBuf) {
        if (byteBuf != null) {
            this.stringBuilder.append(stringOf(byteBuf));
        }
        return this;
    }

    @Override
    public Emitter append(byte[] bytes, int offset, int len) {
        if (bytes != null)
            this.stringBuilder.append(stringOf(Arrays.copyOfRange(bytes, offset, offset + len)));
        return this;
    }

    @Override
    public int length() {
        return this.stringBuilder.length();
    }
}
