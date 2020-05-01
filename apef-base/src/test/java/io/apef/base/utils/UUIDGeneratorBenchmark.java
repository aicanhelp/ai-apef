package io.apef.base.utils;

import io.apef.testing.benchmark.AbstractMicrobenchmark;
import org.openjdk.jmh.annotations.*;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

@Threads(4)
@Warmup(iterations = 0)
@Measurement(iterations = 5)
public class UUIDGeneratorBenchmark extends AbstractMicrobenchmark {

    @State(Scope.Benchmark)
    public static class GeneratorState {
        String uuidString;
        byte[] uuidBytes;
        Set<UUID> uuidSet;

        @org.openjdk.jmh.annotations.Setup(Level.Iteration)
        public void setup() {
            try {
                uuidString = UUIDGenerator.uuidString();
                uuidBytes = UUIDGenerator.uuidBytes();
                uuidSet = new ConcurrentSkipListSet<>();
            } catch (Exception ex) {
                log.error("", ex);
            }
        }
    }

    @Benchmark
    public void benchmarkThreadSafe(GeneratorState generatorState) {
        if (!generatorState.uuidSet.add(UUIDGenerator.uuid())) {
            log.error("--------Failed");
        }
    }

    @Benchmark
    public void benchmarkUuidString() {
        UUIDGenerator.uuidString();
    }

    @Benchmark
    public void benchmarkUuidBytes() {
        UUIDGenerator.uuidBytes();
    }

    @Benchmark
    public void benchmarkUuid() {
        UUIDGenerator.uuid();
    }

    @Benchmark
    public void benchmarkUuidFromString(GeneratorState generatorState) {
        UUIDGenerator.uuid(generatorState.uuidString);
    }

    @Benchmark
    public void benchmarkUuidFromBytes(GeneratorState generatorState) {
        UUIDGenerator.uuid(generatorState.uuidBytes);
    }

    @Benchmark
    public void benchmarkJDKUuidParserFromString(GeneratorState generatorState) {
        UUID.fromString(generatorState.uuidString);
    }
}
