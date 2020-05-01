package io.apef.core.channel.executor.streaming;

import io.apef.core.channel.future.ChannelFuture;
import io.apef.core.channel.future.ChannelFutureImpl;
import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;
import rx.Observable;

import static io.apef.testing.benchmark.Benchmark.benchmark;


public class ChannelFutureBenchmark extends BaseUnitSpec {

    //~ 7000M TPS
    @Test
    public void benchmarkWithoutMapper() {
        benchmark()
                .async(false)
                .concurrency(2000)
                .iterations(10000)
                .rounds(10)
                .benchmarkTask((index, runnerContext) -> {
                    ChannelFutureImpl<Integer> future1 =
                            new ChannelFutureImpl<>();
                    future1.onSuccess(outputValue -> {
                        runnerContext.done(index);
                    });
                    future1.complete(index);
                })
                .start();
    }

    // ~ 1800M TPS
    //Maybe still have the space for improvement
    @Test
    public void benchmarkMapper() {
        benchmark()
                .async(false)
                .concurrency(2000)
                .iterations(10000)
                .rounds(10)
                .benchmarkTask((index, runnerContext) -> {
                    ChannelFutureImpl<Integer> future1 =
                            new ChannelFutureImpl<>();
                    new ChannelStreamImpl<>(future1)
                            .map(value -> ChannelFuture.completeFuture(true))
                            .future()
                            .onSuccess(outputValue -> {
                                runnerContext.done(index);
                            });
                    future1.complete(index);
                })
                .start();
    }

    //~ 1400M TPS
    @Test
    public void benchmarkRxJava() {
        benchmark()
                .async(false)
                .concurrency(2000)
                .iterations(10000)
                .rounds(10)
                .benchmarkTask((index, runnerContext) -> {
                    Observable.just(index)
                            .subscribe(integer -> {
                                runnerContext.done(index);
                            });
                })
                .start();
    }

    // ~ 750M TPS
    @Test
    public void benchmarkRxJavaMap() {
        benchmark()
                .async(false)
                .concurrency(2000)
                .iterations(10000)
                .rounds(10)
                .benchmarkTask((index, runnerContext) -> {
                    Observable.just(index)
                            .map(integer -> index)
                            .subscribe(integer -> {
                                runnerContext.done(index);
                            });
                })
                .start();
    }
}
