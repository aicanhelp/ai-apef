package io.apef.core.channel.executor.batch;

import io.apef.core.channel.executor.ChannelExecutor;
import lombok.Getter;
import lombok.experimental.Accessors;

public interface BatchScheduleTask<T> {
    BatchScheduleTask<T> stopOnFailure(boolean completeOnFail);

    BatchScheduleTask<T> completeOnSuccess(boolean completeOnSuccess);

    BatchSchedule<T> submit(ChannelExecutor.FutureTask<T> futureTask);
}

@Getter
@Accessors(fluent = true)
class BatchScheduleTaskImpl<T> implements BatchScheduleTask<T> {
    private String name = "UnknownTask";
    private boolean stopOnFailure;
    private boolean completeOnSuccess;
    private ChannelExecutor.FutureTask<T> futureTask;
    private BatchSchedule<T> batchSchedule;

    public BatchScheduleTaskImpl(BatchSchedule<T> batchSchedule) {
        this.batchSchedule = batchSchedule;
    }

    public BatchScheduleTaskImpl(String name, BatchSchedule<T> batchSchedule) {
        this.name = name;
        this.batchSchedule = batchSchedule;
    }

    public BatchScheduleTaskImpl<T> stopOnFailure(boolean stopOnFailure) {
        this.stopOnFailure = stopOnFailure;
        return this;
    }

    public BatchScheduleTaskImpl<T> completeOnSuccess(boolean completeOnSuccess) {
        this.completeOnSuccess = completeOnSuccess;
        return this;
    }

    public BatchSchedule<T> submit(ChannelExecutor.FutureTask<T> futureTask) {
        if (futureTask == null) {
            throw new IllegalArgumentException("Submit task can not be null");
        }
        this.futureTask = futureTask;
        return this.batchSchedule;
    }
}