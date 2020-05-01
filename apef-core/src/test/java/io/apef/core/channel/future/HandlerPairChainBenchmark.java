package io.apef.core.channel.future;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;


@Threads(1)
@Warmup(iterations = 0)
@Measurement(iterations = 5)
public class HandlerPairChainBenchmark {

    static SuccessHandler successHandler = new SuccessHandler() {
        @Override
        public void handle(Object outputValue) {

        }
    };

    static ChannelFuture.SuccessHandlerChain handlerPair = new ChannelFuture.SuccessHandlerChain(successHandler);

    @Benchmark
    public void benchmarkArrays() {
        for (int i = 0; i < 1000; i++) {
            handlerPair.next(successHandler);
        }
    }
}