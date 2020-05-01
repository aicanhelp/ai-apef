package io.apef.core.channel.executor.batch;

import io.apef.core.channel.Channel;
import io.apef.core.channel.executor.ChannelExecutor;
import io.apef.core.channel.feature.timeout.TimeoutException;
import io.apef.core.channel.future.ChannelFuture;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class BatchScheduleImpl<T> implements BatchSchedule<T> {

    private final static TimeoutException TIMEOUT_EXCEPTION = new TimeoutException("BatchSchedule timeout");

    private final static Exception TERM_EXCEPTION = new TimeoutException("BatchSchedule terminated");

    @Setter
    @Accessors(fluent = true)
    private int timeout = -1;
    @Setter
    @Accessors(fluent = true)
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    @Setter
    @Accessors(fluent = true)
    private boolean stopOnFailure;
    @Setter
    @Accessors(fluent = true)
    private int concurrency;

    private int finishedCount = 0;

    private int currentExecuting = 0;
    private int executingIndex = 0;

    private T outputContext;
    private List<BatchScheduleTaskImpl<T>> tasks = new ArrayList<>();
    private Channel<?> channel;
    private BatchFutureImpl<T> batchFuture;

    private ChannelExecutor.CompleteSupplier<T> failureSupplier;
    private ChannelExecutor.CompleteSupplier<T> successSupplier;

    public BatchScheduleImpl(Channel<?> channel) {
        this.channel = channel;
    }

    @Override
    public BatchSchedule<T> withOutputContext(T outputContext) {
        this.outputContext = outputContext;
        return this;
    }

    @Override
    public BatchScheduleImpl<T> failureOn(ChannelExecutor.CompleteSupplier<T> failureSupplier) {
        this.failureSupplier = failureSupplier;
        return this;
    }

    @Override
    public BatchScheduleImpl<T> successOn(ChannelExecutor.CompleteSupplier<T> successSupplier) {
        this.successSupplier = successSupplier;
        return this;
    }

    @Override
    public BatchScheduleTask<T> task(String taskName) {
        BatchScheduleTaskImpl<T> taskFuture = new BatchScheduleTaskImpl<>(taskName, this);
        tasks.add(taskFuture);
        return taskFuture;
    }

    @Override
    public BatchScheduleTask<T> task() {
        BatchScheduleTaskImpl<T> taskFuture = new BatchScheduleTaskImpl<>(this);
        tasks.add(taskFuture);
        return taskFuture;
    }

    @Override
    public BatchFuture<T> start() {
        if (this.channel == null) {
            throw new IllegalArgumentException("BusinessChannel is required.");
        }
        if (tasks.isEmpty()) {
            throw new IllegalArgumentException("Task for executed is required.");
        }
        if (this.concurrency <= 0) {
            this.concurrency = this.tasks.size();
        }
        if (this.timeout > 0) {
            this.channel.schedule(this::timeout, this.timeout, this.timeUnit);
        }
        this.batchFuture = new BatchFutureImpl<>();
        this.executeTasks();
        return this.batchFuture;
    }

    private void timeout() {
        if (this.batchFuture.isCompleted()) return;
        this.batchFuture.complete(TIMEOUT_EXCEPTION.getMessage(), TIMEOUT_EXCEPTION);
    }

    private boolean completeOnSupplier(String errMsg, Throwable ex) {
        if (this.successSupplier != null && this.successSupplier.isCompleted(this.outputContext)) {
            this.batchFuture.complete(this.outputContext);
            return true;
        }
        if (this.failureSupplier != null && this.failureSupplier.isCompleted(this.outputContext)) {
            this.batchFuture.complete(errMsg, ex);
            return true;
        }
        return false;
    }

    private boolean complete(BatchScheduleTaskImpl<T> executorTaskFuture) {
        currentExecuting--;
        if (this.batchFuture.isCompleted()) return true;

        if (this.completeOnSupplier(TERM_EXCEPTION.getMessage(), TERM_EXCEPTION)) {
            return true;
        }

        if (executorTaskFuture.completeOnSuccess()) {
            this.batchFuture.complete(this.outputContext);
            return true;
        }

        if (++finishedCount == tasks.size()) {
            this.batchFuture.complete(this.outputContext);
            return true;
        }

        return false;
    }

    private boolean complete(BatchScheduleTaskImpl<T> executorTaskFuture,
                             String errMsg, Throwable cause) {
        currentExecuting--;
        if (this.batchFuture.isCompleted()) return true;

        if (this.completeOnSupplier(errMsg, cause)) {
            return true;
        }

        if (executorTaskFuture.stopOnFailure() || stopOnFailure) {
            this.batchFuture.complete(errMsg, cause);
            return true;
        }

        if (++finishedCount == tasks.size()) {
            this.batchFuture.complete(this.outputContext);
            return true;
        }
        return false;
    }

    private void executeTasks() {
        while (currentExecuting < concurrency && executingIndex < tasks.size()) {
            if (this.batchFuture.isCompleted()) return;
            BatchScheduleTaskImpl<T> executorTaskFuture = tasks.get(executingIndex++);

            ChannelFuture<?> taskFuture;

            try {
                taskFuture = executorTaskFuture.futureTask()
                        .run(this.outputContext);
            } catch (Exception ex) {
                log.error("Exception thrown on run Batch task", ex);
                if (!complete(executorTaskFuture, ex.getMessage(), ex)) {
                    this.executeTasks();
                }
                currentExecuting++;
                return;
            }

            if (taskFuture == null) {
                throw new RuntimeException("The return Future of Batch Task can not be null !!!");
            }

            taskFuture
                    .onSuccess((outputValue) -> {
                        if (!complete(executorTaskFuture)) {
                            this.executeTasks();
                        }
                    })
                    .onFailure((errMsg, cause) -> {
                        if (!complete(executorTaskFuture, errMsg, cause)) {
                            this.executeTasks();
                        }
                    });

            currentExecuting++;
        }
    }
}
