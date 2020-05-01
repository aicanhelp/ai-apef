package io.apef.base.utils;

import io.apef.testing.benchmark.AbstractMicrobenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

import static io.apef.base.utils.Bytes.bytesOf;

@Threads(1)
@Warmup(iterations = 0)
@Measurement(iterations = 5)
public class ResourceBenchmark extends AbstractMicrobenchmark {
    String resourceUrl = "http://localhost:9090/resource.index?param=a";
    byte[] resourceUrlBytes = bytesOf(resourceUrl);

    //~1500M tps/per thread
    @Benchmark
    public void benchmarkResource() {
        new Resource(resourceUrl);
    }

    //~3000M tps/per thread
    @Benchmark
    public void benchmarkBytesResource() {
        new BytesResource(resourceUrlBytes);
    }
}
