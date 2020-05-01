package io.apef.testing.tests.rxjava;

import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;
import rx.Observable;

import static io.apef.testing.benchmark.Benchmark.benchmark;

public class Compare1And2Benchmark extends BaseUnitSpec {

    @Test
    public void testRxJava1Just() {
        //~ 1400M TPS
        benchmark().threads(1).concurrency(3000).iterations(3000).rounds(10)
                .async(false)
                .benchmarkTask((index, runnerContext) -> {
                    Observable.just(index)
                            .subscribe(integer -> {
                                runnerContext.done(index);
                            }, throwable -> {
                                runnerContext.done(index);
                            });
                })
                .start();
    }

    @Test
    public void testRxJava2ObservableJust() {
        // ~ 2500 M TPS
        benchmark().threads(1).concurrency(3000).iterations(3000).rounds(10)
                .async(false)
                .benchmarkTask((index, runnerContext) -> {
                    io.reactivex.Observable.just(index)
                            .subscribe(integer -> {
                                runnerContext.done(index);
                            }, throwable -> {
                                runnerContext.done(index);
                            });
                })
                .start();
    }

    @Test
    public void testRxJava2FlowableJust() {
        // ~ 2200 M TPS
        benchmark().threads(1).concurrency(3000).iterations(3000).rounds(10)
                .async(false)
                .benchmarkTask((index, runnerContext) -> {
                    io.reactivex.Flowable.just(index)
                            .subscribe(integer -> {
                                runnerContext.done(index);
                            }, throwable -> {
                                runnerContext.done(index);
                            });
                })
                .start();
    }

    @Test
    public void testRxJava1Map() {
        //~ 750M TPS
        benchmark().threads(1).concurrency(3000).iterations(3000).rounds(10)
                .async(false)
                .benchmarkTask((index, runnerContext) -> {
                    Observable.just(index)
                            .map(integer -> index)
                            .subscribe(integer -> {
                                runnerContext.done(index);
                            }, throwable -> {
                                runnerContext.done(index);
                            });
                })
                .start();

        // ~ 1500 M TPS
        benchmark().threads(1).concurrency(3000).iterations(3000).rounds(10)
                .async(false)
                .benchmarkTask((index, runnerContext) -> {
                    io.reactivex.Observable.just(index)
                            .map(integer -> index)
                            .subscribe(integer -> {
                                runnerContext.done(index);
                            }, throwable -> {
                                runnerContext.done(index);
                            });
                })
                .start();

        // ~ 1500 M TPS
        benchmark().threads(1).concurrency(3000).iterations(3000).rounds(10)
                .async(false)
                .benchmarkTask((index, runnerContext) -> {
                    io.reactivex.Flowable.just(index)
                            .map(integer -> index)
                            .subscribe(integer -> {
                                runnerContext.done(index);
                            }, throwable -> {
                                runnerContext.done(index);
                            });
                })
                .start();
    }

    @Test
    public void testRxJava2ObservableMap() {
        // ~ 1500 M TPS
        benchmark().threads(1).concurrency(3000).iterations(3000).rounds(10)
                .async(false)
                .benchmarkTask((index, runnerContext) -> {
                    io.reactivex.Observable.just(index)
                            .map(integer -> index)
                            .subscribe(integer -> {
                                runnerContext.done(index);
                            }, throwable -> {
                                runnerContext.done(index);
                            });
                })
                .start();
    }

    @Test
    public void testRxJava2FlowableMap() {

        // ~ 1250 M TPS
        benchmark().threads(1).concurrency(3000).iterations(3000).rounds(10)
                .async(false)
                .benchmarkTask((index, runnerContext) -> {
                    io.reactivex.Flowable.just(index)
                            .map(integer -> index)
                            .subscribe(integer -> {
                                runnerContext.done(index);
                            }, throwable -> {
                                runnerContext.done(index);
                            });
                })
                .start();
    }
}
