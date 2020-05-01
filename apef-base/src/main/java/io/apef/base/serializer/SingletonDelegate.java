package io.apef.base.serializer;

import io.protostuff.Input;
import io.protostuff.Output;
import io.protostuff.Pipe;
import io.protostuff.WireFormat.FieldType;
import io.protostuff.runtime.Delegate;
import lombok.AllArgsConstructor;

import java.io.IOException;

@AllArgsConstructor
public class SingletonDelegate<T> implements Delegate<T> {
    private Class<? super T> aClass;
    private Encoder<T> encoder;
    private Decoder<T> decoder;

    @Override
    public FieldType getFieldType() {
        return FieldType.BYTES;
    }

    @Override
    public T readFrom(Input input) throws IOException {
        return this.decoder.decode(input.readByteArray());
    }

    @Override
    public void writeTo(Output output, int i, T t, boolean b) throws IOException {
        output.writeByteArray(i, this.encoder.encode(t), b);
    }

    @Override
    public void transfer(Pipe pipe, Input input, Output output, int i, boolean b) throws IOException {
        input.transferByteRangeTo(output, false, i, b);
    }

    @Override
    public Class<?> typeClass() {
        return this.aClass;
    }


    public interface Encoder<T> {
        byte[] encode(T o);
    }

    public interface Decoder<T> {
        T decode(byte[] value);
    }
}
