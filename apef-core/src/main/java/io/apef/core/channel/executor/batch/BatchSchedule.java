package io.apef.core.channel.executor.batch;

import io.apef.core.channel.executor.ChannelExecutor;

import java.util.concurrent.TimeUnit;

public interface BatchSchedule<T> {
    BatchFuture<T> start();

    BatchScheduleTask<T> task();

    BatchScheduleTask<T> task(String taskName);

    BatchSchedule<T> withOutputContext(T outputContext);

    BatchSchedule<T> timeout(int timeout);

    BatchSchedule<T> timeUnit(TimeUnit timeUnit);

    BatchSchedule<T> concurrency(int concurrency);

    BatchSchedule<T> stopOnFailure(boolean stopOnFailure);

    BatchSchedule<T> failureOn(ChannelExecutor.CompleteSupplier<T> completeSupplier);

    BatchSchedule<T> successOn(ChannelExecutor.CompleteSupplier<T> completeSupplier);
}
