package io.apef.core.channel.executor.schedule;

import io.apef.core.channel.executor.ChannelExecutor;

import java.util.concurrent.TimeUnit;

public interface ChannelSchedule<T> {
    ChannelSchedule<T> delay(int delay);

    ChannelSchedule<T> times(int times);

    ChannelSchedule<T> interval(int interval);

    ChannelSchedule<T> timeout(int timeout);

    ChannelSchedule<T> timeUnit(TimeUnit timeUnit);

    ChannelSchedule<T> syncTask(boolean syncTask);

    ChannelSchedule<T> withOutputContext(T outputContext);

    ChannelSchedule<T> stopOnFailure(boolean stopOnFailure);

    ChannelSchedule<T> failOnOverTimes(boolean failOnOverTimes);

    ChannelSchedule<T> completeOnSuccess(boolean completeOnSuccess);

    ChannelSchedule<T> onOverTimes(ChannelExecutor.OnOverTimes<T> onOverTimes);

    ChannelSchedule<T> completeOn(ChannelExecutor.CompleteSupplier<T> completeSupplier);

    ChannelScheduleFuture<T> submit(ChannelExecutor.FutureTask<T> futureTask);
}
