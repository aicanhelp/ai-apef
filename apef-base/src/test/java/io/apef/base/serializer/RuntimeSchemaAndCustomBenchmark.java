package io.apef.base.serializer;

import io.apef.testing.benchmark.AbstractMicrobenchmark;
import io.protostuff.CustomSchema;
import io.protostuff.Schema;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jmh.annotations.*;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

@Threads(1)
@Warmup(iterations = 0)
@Measurement(iterations = 5)
@Slf4j
public class RuntimeSchemaAndCustomBenchmark extends AbstractMicrobenchmark {

    @Test
    public void testGeneral() throws IOException {
        DataSerializer<Entity> dataSerializer = DataSerializer.protoStuff(Entity.class);

        Entity entity = new Entity();
        byte[] data = dataSerializer.serialize(entity);
        assertTrue(data.length > 20);
    }

    @Test
    public void testWithDelegate() throws IOException {
        DataSerializer.registerProtoStuffDelegate(Field.class, o -> {
            byte[] bytes = new byte[1];
            bytes[0] = o.id;
            return bytes;
        }, value -> from(value[0]));
        DataSerializer<Entity> dataSerializer = DataSerializer.protoStuff(Entity.class);

        Entity entity = new Entity();
        byte[] data = dataSerializer.serialize(entity);
        assertTrue(data.length < 20);
        assertTrue(dataSerializer.deserialize(data).field1.id == (byte) 0);
    }

    @State(Scope.Benchmark)
    public static class GeneratorState {
        DataSerializer<Entity> dataSerializer = DataSerializer.protoStuff(Entity.class);
        byte[] data;
        Entity entity;

        @org.openjdk.jmh.annotations.Setup(Level.Iteration)
        public void setup() throws IOException {
            entity = new Entity();
            data = dataSerializer.serialize(entity);

            registerDelegate();
        }

        private void registerDelegate() throws IOException {
            DataSerializer.registerProtoStuffDelegate(Field.class, o -> {
                byte[] bytes = new byte[1];
                bytes[0] = o.id;
                return bytes;
            }, value -> from(value[0]));
        }

        public void serialize() throws IOException {
            dataSerializer.serialize(entity);
        }

        public void deserialize() throws IOException {
            dataSerializer.deserialize(data);
        }
    }

    //~ 200M tps/thread
    @Benchmark
    public void benchmark1(GeneratorState state) throws IOException {
        state.serialize();
    }

    //~ 190M tps/thread
    @Benchmark
    public void benchmark2(GeneratorState state) throws IOException {
        state.deserialize();
    }


    public static class Entity {
        private Field field1 = from((byte) 0);
        private Field field2 = from((byte) 1);
        private int value;
        private String name;
    }

    public static class FieldSchema extends CustomSchema<Field> {

        public FieldSchema(Schema<Field> schema) {
            super(schema);
        }
    }

    public static class Field {
        private String name = "123456789";
        private byte id;
        private int value;

    }

    private static Field from(byte id) {
        return fields.get(id);
    }

    private static Map<Byte, Field> fields = new HashMap<>();

    static {
        for (int i = 0; i < 10; i++) {
            fields.put((byte) i, new Field());
        }
    }
}
