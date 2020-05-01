package io.apef.base.utils;

import io.apef.testing.benchmark.AbstractMicrobenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@Threads(1)
@Warmup(iterations = 0)
@Measurement(iterations = 5)
public class ExpirationBenchmark extends AbstractMicrobenchmark {
    private Expiration expiration = new Expiration(3600 * 4, 900);
    //~~ 47000M tps/thread
    @Benchmark
    public void benchmarkThreadSafe() {

        for (int i = 0; i < 10000; i++) {
            int time = expiration.expirationSecs();
            Expiration.isExpired(time);
        }
    }
}
