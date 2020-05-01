package io.apef.base.utils;

import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class LZ4 {
    static LZ4Compressor compressor;
    static LZ4FastDecompressor decompressor;

    static {
        LZ4Factory factory = LZ4Factory.fastestInstance();
        compressor = factory.fastCompressor();
        decompressor = factory.fastDecompressor();
    }

    static void setSize(byte[] buf, int len) {
        buf[0] = (byte) (len & 0xff);
        buf[1] = (byte) (len >> 8 & 0xff);
        buf[2] = (byte) (len >> 16 & 0xff);
        buf[3] = (byte) (len >> 24 & 0xff);
    }

    static int getSize(byte[] buf) {
        int len = (buf[0] & 0xff) | ((buf[1] & 0xff) << 8) | ((buf[2] & 0xff) << 16) | (buf[3] << 24);
        return len;

    }

    static int getSize(byte[] buf, int offset) {
        int len = (buf[offset] & 0xff) |
                ((buf[offset + 1] & 0xff) << 8) |
                ((buf[offset + 2] & 0xff) << 16) |
                (buf[offset + 3] << 24);
        return len;

    }

    static ThreadLocal<ByteBuffer> localBuffers = new ThreadLocal<ByteBuffer>() {
        public ByteBuffer initialValue() {
            return ByteBuffer.wrap(new byte[1 << 20]);
        }
    };

    public static byte[] compress(byte[] data) {
        byte[] buf = localBuffers.get().array();
        int len = compressor.maxCompressedLength(data.length);
        if (len + 4 > buf.length) buf = new byte[len + 4];
        len = compressor.compress(data, 0, data.length, buf, 4);
        setSize(buf, data.length);
        return Arrays.copyOf(buf, len + 4);
    }

    public static byte[] decompress(byte[] data) {
        byte[] buf = localBuffers.get().array();
        int len = getSize(data);
        if (len > buf.length) buf = new byte[len];
        decompressor.decompress(data, 4, buf, 0, len);
        return Arrays.copyOf(buf, len);
    }

    public static byte[] decompress(byte[] data, int offset) {
        byte[] buf = localBuffers.get().array();
        int destLen = getSize(data, offset);
        if (destLen > buf.length) buf = new byte[destLen];
        decompressor.decompress(data, offset + 4, buf, 0, destLen);
        return Arrays.copyOf(buf, destLen);
    }

    public static byte[] decompress2(byte[] data) {
        int len = getSize(data);
        byte[] buf = new byte[len];
        decompressor.decompress(data, 4, buf, 0, len);
        return buf;
    }
}