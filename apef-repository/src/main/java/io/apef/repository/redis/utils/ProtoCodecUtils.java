package io.apef.repository.redis.utils;

import io.protostuff.GraphIOUtil;
import io.protostuff.LinkedBuffer;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

import java.io.ByteArrayOutputStream;

public class ProtoCodecUtils {
    public static byte[] buildValue(Object o) {
        return build(new Value(o), valueSchema);
    }

    public static byte[] buildKey(Object o) {
        return build(new Key(o), keySchema);
    }

    public static <T> T buildKey(byte[] data) {
        return (T) build(data, keySchema).key;
    }

    public static <T> T buildValue(byte[] data) {
        return (T) build(data, valueSchema).value;
    }

    public static <T> byte[] build(T o, Schema<T> schema) {
        ByteArrayOutputStream temp = new ByteArrayOutputStream();

        LinkedBuffer buffer = LinkedBuffer.allocate();
        try {
            GraphIOUtil.writeTo(temp, o, schema, buffer);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to encode value: " + o, ex);
        }
        return temp.toByteArray();
    }

    public static <T> T build(byte[] data, Schema<T> schema) {
        T value = schema.newMessage();
        GraphIOUtil.mergeFrom(data, value, schema);
        return value;
    }

    static class Key {
        Object key;

        Key(Object key) {
            this.key = key;
        }
    }

    static class Value {
        Object value;

        Value(Object value) {
            this.value = value;
        }
    }

    private final static Schema<Key> keySchema = RuntimeSchema.createFrom(Key.class);
    private final static Schema<Value> valueSchema = RuntimeSchema.createFrom(Value.class);
}
