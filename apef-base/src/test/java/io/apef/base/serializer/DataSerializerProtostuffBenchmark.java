package io.apef.base.serializer;

import io.apef.testing.benchmark.AbstractMicrobenchmark;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.openjdk.jmh.annotations.*;

@Threads(1)
@Warmup(iterations = 0)
@Measurement(iterations = 5)
public class DataSerializerProtostuffBenchmark extends AbstractMicrobenchmark {
    @State(Scope.Benchmark)
    public static class GeneratorState {
        TestObject object = new TestObject();
        DataSerializer<TestObject> dataSerializer1 = DataSerializer.protoStuff(TestObject.class);

        private void serializer1() {
            try {
                dataSerializer1.serialize(object);
            } catch (Exception ex) {
                log.error("", ex);
            }
        }

        private void serializer2() {
            ByteBuf data = Unpooled.buffer(10);
            data.writeInt(99);
            try {
                dataSerializer1.serializeTo(object, data);
            } catch (Exception ex) {
                log.error("", ex);
            }
            data.array();
        }

        private void serializer3() {
            ByteBuf data = Unpooled.buffer(1024);
            data.writeInt(99);
            try {
                data.writeBytes(dataSerializer1.serialize(object));
            } catch (Exception ex) {
                log.error("", ex);
            }
            data.getBytes(0, new byte[data.readableBytes()]);
        }
    }


    public static class TestObject {
        byte[] data = new byte[1000];
    }

    //~ 200M tps/thread
    @Benchmark
    public void benchmark1(GeneratorState state) {
        state.serializer1();
    }

    //~ 190M tps/thread
    @Benchmark
    public void benchmark2(GeneratorState state) {
        state.serializer2();
    }

    //~ 100M tps/thread
    @Benchmark
    public void benchmark3(GeneratorState state) {
        state.serializer3();
    }
}
