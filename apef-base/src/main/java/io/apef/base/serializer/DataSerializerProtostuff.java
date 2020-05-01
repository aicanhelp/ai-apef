package io.apef.base.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.protostuff.*;
import io.protostuff.runtime.*;

import java.io.IOException;
import java.io.OutputStream;

public class DataSerializerProtostuff<T> implements DataSerializer<T> {

    private Schema<T> schema;
    private int defaultAllocateSize = 1024;

    protected DataSerializerProtostuff(Class<T> domainClass) {
        this.schema = RuntimeSchema.getSchema(domainClass);
    }

    protected DataSerializerProtostuff(Class<T> domainClass, IdStrategy idStrategy) {
        this.schema = RuntimeSchema.getSchema(domainClass, idStrategy);
    }

    protected DataSerializerProtostuff(Class<T> domainClass, Delegate... delegates) {
        registerDelegate(delegates);
        this.schema = RuntimeSchema.getSchema(domainClass);
    }

    protected void registerDelegate(Delegate... delegates) {
        if (delegates != null) {
            for (Delegate delegate : delegates) {
                ((DefaultIdStrategy) RuntimeEnv.ID_STRATEGY).registerDelegate(delegate);
            }
        }
    }

    @Override
    public byte[] serialize(T object) throws IOException {
        return GraphIOUtil.toByteArray(object,
                schema, LinkedBuffer.allocate(defaultAllocateSize));
    }

    @Override
    public T deserialize(byte[] data) throws IOException {
        T object = schema.newMessage();
        GraphIOUtil.mergeFrom(data, object, schema);
        return object;
    }

    @Override
    public void serializeTo(T object, ByteBuf byteBuf) throws IOException {
        LinkedBuffer linkedBuffer = LinkedBuffer.allocate(defaultAllocateSize);
        int size = GraphIOUtil.writeTo(linkedBuffer, object, schema);

        byteBuf.capacity(byteBuf.readableBytes() + size);

        ByteBufOutputStream bufOutputStream = new ByteBufOutputStream(byteBuf);

        LinkedBuffer.writeTo((OutputStream) bufOutputStream, linkedBuffer);
    }

    @Override
    public byte[] serialize(T object, ByteBuf byteBuf) throws IOException {
        serializeTo(object, byteBuf);
        return byteBuf.array();
    }

    @Override
    public T deserialize(byte[] data, int start, int length) throws IOException {
        T object = schema.newMessage();
        GraphIOUtil.mergeFrom(data, start, length, object, schema);
        return object;
    }
}
