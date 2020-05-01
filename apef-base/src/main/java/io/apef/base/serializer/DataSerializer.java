package io.apef.base.serializer;

import io.netty.buffer.ByteBuf;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.Delegate;
import io.protostuff.runtime.RuntimeEnv;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface DataSerializer<T> {
    Map<Class, DataSerializer> dataSerializerMap = new ConcurrentHashMap<>();

    byte[] serialize(T object) throws IOException;

    T deserialize(byte[] data) throws IOException;

    void serializeTo(T object, ByteBuf byteBuf) throws IOException;

    byte[] serialize(T object, ByteBuf byteBuf) throws IOException;

    T deserialize(byte[] data, int start, int length) throws IOException;

    static <T> DataSerializer<T> protoStuff(Class<T> tClass) {
        synchronized (dataSerializerMap) {
            DataSerializer<T> dataSerializer = dataSerializerMap.get(tClass);

            if (dataSerializer == null) {
                dataSerializer = new DataSerializerProtostuff<T>(tClass);
                dataSerializerMap.put(tClass, dataSerializer);
            }
            return dataSerializer;
        }
    }

    static void registerProtoStuffDelegate(Delegate... delegates) {
        if (delegates != null) {
            for (Delegate delegate : delegates) {
                ((DefaultIdStrategy) RuntimeEnv.ID_STRATEGY).registerDelegate(delegate);
            }
        }
    }

    static <T> void registerProtoStuffDelegate(Class<? super T> aClass,
                                               SingletonDelegate.Encoder<T> encoder,
                                               SingletonDelegate.Decoder<T> decoder) {
        ((DefaultIdStrategy) RuntimeEnv.ID_STRATEGY).registerDelegate(new SingletonDelegate<>(aClass, encoder, decoder));
    }

    static <T> DataSerializer<T> protoStuff(Class<T> tClass, Delegate... delegates) {
        synchronized (dataSerializerMap) {
            DataSerializer<T> dataSerializer = dataSerializerMap.get(tClass);

            if (dataSerializer == null) {
                dataSerializer = new DataSerializerProtostuff<T>(tClass, delegates);
                dataSerializerMap.put(tClass, dataSerializer);
            } else {
                if (delegates != null) {
                    ((DataSerializerProtostuff) dataSerializer).registerDelegate(delegates);
                }
            }
            return dataSerializer;
        }
    }
}
