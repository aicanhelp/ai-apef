package io.apef.base.utils;

import com.fasterxml.uuid.impl.UUIDUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.AppendableCharSequence;

import java.nio.charset.Charset;
import java.util.UUID;

public class UUIDGenerator {
    public static UUID uuid() {
        com.eaio.uuid.UUID uuid = new com.eaio.uuid.UUID();
        AppendableCharSequence appendableCharSequence = new AppendableCharSequence(36);
        uuid.toAppendable(appendableCharSequence);
        return UUIDUtil.uuid(appendableCharSequence.toString());
    }

    public static byte[] uuidBytes() {
        return uuidBytes(Charset.defaultCharset());
    }

    public static byte[] uuidBytes(Charset charset) {
        ByteBuf byteBuf = uuidByteBuf(charset);
        byte[] data = new byte[36];
        byteBuf.readBytes(data);
        return data;
    }

    public static ByteBuf uuidByteBuf() {
        return uuidByteBuf(Charset.defaultCharset());
    }

    public static ByteBuf uuidByteBuf(Charset charset) {
        com.eaio.uuid.UUID uuid = new com.eaio.uuid.UUID();
        AppendableCharSequence appendableCharSequence = new AppendableCharSequence(36);
        uuid.toAppendable(appendableCharSequence);
        ByteBuf byteBuf = Unpooled.buffer(36);
        byteBuf.writeCharSequence(appendableCharSequence, charset);
        return byteBuf;
    }

    public static String uuidString() {
        com.eaio.uuid.UUID uuid = new com.eaio.uuid.UUID();
        AppendableCharSequence appendableCharSequence = new AppendableCharSequence(36);
        uuid.toAppendable(appendableCharSequence);
        return appendableCharSequence.toString();
    }

    public static UUID uuid(String uuidString) {
        return UUIDUtil.uuid(uuidString);
    }

    public static UUID uuid(byte[] uuidStringBytes) {
        return UUIDUtil.uuid(new String(uuidStringBytes));
    }

    public static UUID uuid(byte[] uuidStringBytes, Charset charset) {
        return UUIDUtil.uuid(new String(uuidStringBytes, charset));
    }
}
