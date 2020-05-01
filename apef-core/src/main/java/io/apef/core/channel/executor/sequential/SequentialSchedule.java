package io.apef.core.channel.executor.sequential;

import io.apef.core.channel.executor.ChannelExecutor;

import java.util.concurrent.TimeUnit;

public interface SequentialSchedule<T> {
    SequentialFuture<T> start();

    SequentialSchedule<T> withOutputContext(T outputContext);

    SequentialSchedule<T> timeout(int timeout);

    SequentialSchedule<T> timeUnit(TimeUnit timeUnit);

    SequentialSchedule<T> stopOnFailure(boolean stopOnFailure);

    SequentialSchedule<T> addTaskToTail(ChannelExecutor.FutureTask<T> futureTask);

    SequentialSchedule<T> failureOn(ChannelExecutor.CompleteSupplier<T> completeSupplier);

    SequentialSchedule<T> successOn(ChannelExecutor.CompleteSupplier<T> completeSupplier);
}
