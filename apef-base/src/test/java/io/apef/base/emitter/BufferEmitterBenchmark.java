package io.apef.base.emitter;

import io.apef.testing.benchmark.AbstractMicrobenchmark;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.core.buffer.Buffer;
import org.openjdk.jmh.annotations.*;

@Threads(1)
@Warmup(iterations = 0)
@Measurement(iterations = 10)
public class BufferEmitterBenchmark extends AbstractMicrobenchmark {
    @State(Scope.Benchmark)
    public static class BenchmarkState {
        byte[] data = "1234567891023332211".getBytes();
        ByteBuf buf = Unpooled.wrappedBuffer(data);
        Buffer buffer = Buffer.buffer(data);
        byte[] dataSlice = "1234567890".getBytes();
    }

//    @Benchmark
    public void benchmarkAppendByteArray(BenchmarkState state) {
        BufferEmitter bufferEmitter = new BufferEmitter(12800);
        for (int i = 0; i < 100; i++) {
            byte[] slice = new byte[10];
            state.buf.getBytes(5, slice);
            bufferEmitter.append(slice);
        }
    }

//    @Benchmark
    public void benchmarkAppendByteArray2(BenchmarkState state) {
        BufferEmitter bufferEmitter = new BufferEmitter(12800);
        for (int i = 0; i < 100; i++) {
            bufferEmitter.append(state.dataSlice);
        }
    }

//    @Benchmark
    public void benchmarkAppendByteBuf(BenchmarkState state) {
        BufferEmitter bufferEmitter = new BufferEmitter(12800);
        for (int i = 0; i < 100; i++) {
            bufferEmitter.append(state.buf.slice(5, 10));
        }
    }

//    @Benchmark
    public void benchmarkAppendBuffer(BenchmarkState state) {
        BufferEmitter bufferEmitter = new BufferEmitter(12800);
        for (int i = 0; i < 100; i++) {
            bufferEmitter.append(state.buffer.slice(5, 10));
        }
    }

//    @Benchmark
    public void benchmarkAppendBytesOffset(BenchmarkState state) {
        BufferEmitter bufferEmitter = new BufferEmitter(12800);
        for (int i = 0; i < 100; i++) {
            bufferEmitter.append(state.data, 5, 10);
        }
    }
}