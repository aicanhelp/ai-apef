package io.apef.testing.benchmark;

import org.testng.annotations.Test;


public class BenchmarkContextTest {
    @Test
    public void test() {
        new BenchmarkContext()
                .async(false)
                .concurrency(4000).iterations(1000)
                .benchmarkTask((index, runnerContext) -> {
                    runnerContext.done(index);
                })
                .start();
    }
}