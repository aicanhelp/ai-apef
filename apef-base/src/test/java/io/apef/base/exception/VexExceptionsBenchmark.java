package io.apef.base.exception;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@Threads(1)
@Warmup(iterations = 0)
@Measurement(iterations = 5)
public class VexExceptionsBenchmark {
    @Benchmark
    public void benchmarkOf() {
        for (int i = 0; i < 10000; i++) {
            VexExceptions.of(401);
        }
    }

    @Benchmark
    public void benchmarkOfUnknow() {
        for (int i = 0; i < 10000; i++) {
            VexExceptions.of(900);
        }
    }

    private static Throwable throwable = new Exception("test");

    @Benchmark
    public void benchmarkOfException() {
        for (int i = 0; i < 10000; i++) {
            VexExceptions.of(throwable);
        }
    }
}