package io.apef.base.utils;

import io.apef.testing.benchmark.AbstractMicrobenchmark;
import org.apache.commons.lang3.StringUtils;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;

@Threads(1)
@Warmup(iterations = 0)
@Measurement(iterations = 5)
public class StrUtilsBenchmark extends AbstractMicrobenchmark {

    private final static String source = "434343439049030ueoiffoabcdef,joijdlkfjdlabcdefffdfd";
    private final static String target = "abcdef";

    //About 6000M tps
    @Benchmark
    public void benchmarkStringUtilsContains() {
        for (int i = 0; i < 10000; i++) {
            StringUtils.contains(source, target);
        }
    }

    //About 32000M tps
    @Benchmark
    public void benchmarkStrUtilsContains() {
        for (int i = 0; i < 10000; i++) {
            StrUtils.indexOf(source, target, 16, 30);
        }
    }
}
