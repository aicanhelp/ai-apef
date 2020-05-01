package io.apef.core.channel.executor.sequential;


import io.apef.core.channel.Channel;
import io.apef.core.channel.executor.ChannelExecutor;
import io.apef.core.channel.feature.timeout.TimeoutException;
import io.apef.core.channel.future.ChannelFuture;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class SequentialScheduleImpl<T> implements SequentialSchedule<T> {
    private final static TimeoutException TIMEOUT_EXCEPTION = new TimeoutException("SequentialSchedule timeout");

    private final static Exception TERM_EXCEPTION = new TimeoutException("SequentialSchedule terminated");

    @Setter
    @Accessors(fluent = true)
    private int timeout = -1;
    @Setter
    @Accessors(fluent = true)
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    @Setter
    @Accessors(fluent = true)
    private boolean stopOnFailure;

    private ChannelExecutor.CompleteSupplier<T> failureSupplier;
    private ChannelExecutor.CompleteSupplier<T> successSupplier;

    private T outputContext;
    private Channel<?> channel;
    private SequentialFutureImpl<T> sequentialFuture;
    private TaskChain<T> taskChain;

    public SequentialScheduleImpl(Channel<?> channel) {
        this.channel = channel;
    }

    @Override
    public SequentialScheduleImpl<T> failureOn(ChannelExecutor.CompleteSupplier<T> failureSupplier) {
        this.failureSupplier = failureSupplier;
        return this;
    }

    @Override
    public SequentialScheduleImpl<T> successOn(ChannelExecutor.CompleteSupplier<T> successSupplier) {
        this.successSupplier = successSupplier;
        return this;
    }

    @Override
    public SequentialFuture<T> start() {
        if (this.channel == null) {
            throw new IllegalArgumentException("BusinessChannel is required.");
        }
        if (taskChain == null) {
            throw new IllegalArgumentException("Task for executed is required.");
        }

        if (this.timeout > 0) {
            this.channel.schedule(this::timeout, this.timeout, this.timeUnit);
        }
        this.sequentialFuture = new SequentialFutureImpl<T>();
        this.runTask(this.taskChain.top);
        return this.sequentialFuture;
    }

    private void timeout() {
        if (this.sequentialFuture.isCompleted()) return;
        this.sequentialFuture.complete(TIMEOUT_EXCEPTION.getMessage(), TIMEOUT_EXCEPTION);
    }

    private boolean completeOnSupplier(String errMsg, Throwable ex) {
        if (this.successSupplier != null && this.successSupplier.isCompleted(this.outputContext)) {
            this.sequentialFuture.complete(this.outputContext);
            return true;
        }
        if (this.failureSupplier != null && this.failureSupplier.isCompleted(this.outputContext)) {
            this.sequentialFuture.complete(errMsg, ex);
            return true;
        }
        return false;
    }

    private boolean complete(String errMsg, Throwable cause) {
        if (this.sequentialFuture.isCompleted()) return true;

        if (this.completeOnSupplier(errMsg, cause)) {
            return true;
        }

        if (this.stopOnFailure) {
            this.sequentialFuture.complete(errMsg, cause);
            return true;
        }

        return false;
    }

    private boolean complete(TaskChain<T> taskChain) {
        if (this.sequentialFuture.isCompleted()) return true;

        if (taskChain == null) {
            this.sequentialFuture.complete(this.outputContext);
            return true;
        }

        if (this.completeOnSupplier(TERM_EXCEPTION.getMessage(), TERM_EXCEPTION)) {
            return true;
        }
        return false;
    }

    private void runTask(TaskChain<T> taskChain) {
        if (this.complete(taskChain)) return;

        ChannelFuture<?> future;

        try {
            future = taskChain.item.run(this.outputContext);
        } catch (Exception ex) {
            log.error("Exception thrown on run Sequential task", ex);
            if (!complete(ex.getMessage(), ex)) {
                runTask(taskChain.next);
            }
            return;
        }

        if (future == null) {
            throw new RuntimeException("The return Future of Sequential Task can not be null !!!");
        }

        future.onSuccess((outputValue) -> {
            runTask(taskChain.next);
        }).onFailure((errMsg, cause) -> {
            if (!complete(errMsg, cause)) {
                runTask(taskChain.next);
            }
        });
    }

    @Override
    public SequentialSchedule<T> withOutputContext(T outputContext) {
        this.outputContext = outputContext;
        return this;
    }

    @Override
    public SequentialSchedule<T> addTaskToTail(ChannelExecutor.FutureTask<T> futureTask) {
        if (futureTask == null) return this;
        if (this.taskChain == null) {
            this.taskChain = new TaskChain<>(futureTask);
        } else {
            this.taskChain = this.taskChain.next(futureTask);
        }
        return this;
    }

    private static class TaskChain<T> {
        TaskChain<T> top;
        ChannelExecutor.FutureTask<T> item;
        TaskChain<T> next;

        TaskChain(ChannelExecutor.FutureTask<T> item) {
            this.item = item;
            this.top = this;
        }

        public TaskChain<T> next(ChannelExecutor.FutureTask<T> next) {
            this.next = new TaskChain<>(next);
            this.next.top = this.top;
            return this.next;
        }
    }
}
