package io.apef.core.channel.executor.schedule;


import io.apef.core.channel.Channel;
import io.apef.core.channel.executor.ChannelExecutor;
import io.apef.core.channel.executor.ChannelScheduleCancelException;
import io.apef.core.channel.feature.timeout.TimeoutException;
import io.apef.core.channel.future.ChannelFuture;
import io.apef.core.utils.ChannelTimer;
import io.netty.util.Timeout;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

//todo: need to refactor to create a executor to execute same condition tasks
@Slf4j
public class ChannelScheduleImpl<T> implements ChannelSchedule<T>, Runnable {
    private final static TimeoutException TIMEOUT_EXCEPTION = new TimeoutException("ChannelSchedule Timeout");
    private final static ChannelScheduleCancelException OVER_TIMES_EXCEPTION
            = new ChannelScheduleCancelException("ChannelSchedule Over times");
    private final static ChannelScheduleCancelException CANCEL_EXCEPTION = new ChannelScheduleCancelException();

    @Setter
    @Accessors(fluent = true)
    private int delay = 0;
    @Setter
    @Accessors(fluent = true)
    private int timeout = -1;
    @Setter
    @Accessors(fluent = true)
    private TimeUnit timeUnit = TimeUnit.SECONDS;
    @Setter
    @Accessors(fluent = true)
    private boolean completeOnSuccess;
    @Setter
    @Accessors(fluent = true)
    private boolean stopOnFailure;
    @Setter
    @Accessors(fluent = true)
    private boolean failOnOverTimes;

    @Setter
    @Accessors(fluent = true)
    private int times = -1;
    @Setter
    @Accessors(fluent = true)
    private int interval = -1;
    @Setter
    @Accessors(fluent = true)
    private boolean syncTask = false;

    private ChannelExecutor.CompleteSupplier<T> completeSupplier;
    @Setter
    @Accessors(fluent = true)
    private ChannelExecutor.OnOverTimes<T> onOverTimes;

    private T ouputContext;

    private Timeout timerContext;
    private int currentTimes = 1;

    private ChannelExecutor.FutureTask<T> futureTask;

    private ChannelScheduleFutureImpl<T> scheduleFuture;

    private Channel<?> channel;

    public ChannelScheduleImpl(Channel<?> channel) {
        this.channel = channel;
    }

    @Override
    public ChannelSchedule<T> withOutputContext(T outputContext) {
        this.ouputContext = outputContext;
        return this;
    }

    @Override
    public ChannelScheduleFuture<T> submit(ChannelExecutor.FutureTask<T> futureTask) {
        if (this.futureTask != null) {
            throw new IllegalArgumentException("Only one FutureTask can be submit.");
        }
        this.futureTask = futureTask;

        this.scheduleFuture = new ChannelScheduleFutureImpl<T>(this);
        if (this.timeout > 0) {
            this.channel.schedule(this::timeout, this.timeout, this.timeUnit);
        }
        if (this.delay > 0) {
            this.channel.schedule(this, this.delay, this.timeUnit);
        } else {
            //for syncTask, let businessChannel to execute it to avoid the stack overload
            if (this.syncTask)
                this.channel.execute(this);
            else
                run();
        }
        return this.scheduleFuture;
    }

    @Override
    public ChannelSchedule<T> completeOn(ChannelExecutor.CompleteSupplier<T> completeSupplier) {
        this.completeSupplier = completeSupplier;
        return this;
    }

    private void timeout() {
        if (this.scheduleFuture.isCompleted()) return;
        this.scheduleFuture.complete(TIMEOUT_EXCEPTION.getMessage(), TIMEOUT_EXCEPTION);
    }

    protected void next() {
        if (this.interval > 0) {
            this.timerContext = ChannelTimer.timer()
                    .newTimeout(timeout -> channel.execute(this),
                            this.interval, this.timeUnit);
        } else {
            if (this.syncTask)
                this.channel.execute(this);
            else
                run();
        }
    }

    private boolean complete(String errMsg, Throwable cause) {
        if (this.scheduleFuture.isCompleted()) return true;

        if (this.completeSupplier != null && this.completeSupplier.isCompleted(this.ouputContext)) {
            this.scheduleFuture.complete(errMsg, cause);
            return true;
        }

        if ((this.times > 0 && this.currentTimes++ >= this.times) || this.stopOnFailure) {
            if (this.onOverTimes != null)
                this.onOverTimes.onOverTimes(this.ouputContext);
            this.scheduleFuture.complete(errMsg, cause);
            return true;
        }

        return false;
    }

    private boolean complete() {
        if (this.scheduleFuture.isCompleted()) return true;

        if (this.completeSupplier != null && this.completeSupplier.isCompleted(this.ouputContext)) {
            this.scheduleFuture.complete(this.ouputContext);
            return true;
        }

        if (this.times > 0 && this.currentTimes++ >= this.times) {
            if (this.onOverTimes != null)
                this.onOverTimes.onOverTimes(this.ouputContext);

            if (this.failOnOverTimes)
                this.scheduleFuture.complete(OVER_TIMES_EXCEPTION.getMessage(), OVER_TIMES_EXCEPTION);
            else
                this.scheduleFuture.complete(this.ouputContext);
            return true;
        }

        if (this.completeOnSuccess) {
            this.scheduleFuture.complete(this.ouputContext);
            return true;
        }

        return false;
    }

    public void run() {
        if (this.scheduleFuture.isCompleted()) return;

        ChannelFuture<?> taskFuture;

        try {
            taskFuture = this.futureTask.run(this.ouputContext);
        } catch (Exception ex) {
            log.error("Failed on run ChannelSchedule task", ex);
            if (!complete(ex.getMessage(), ex)) {
                next();
            }
            return;
        }

        if (taskFuture == null) {
            throw new RuntimeException("The return Future of Scheduled Task can not be null !!!");
        }

        taskFuture
                .onFailure((errMsg, cause) -> {
                    if (!complete(errMsg, cause)) {
                        next();
                    }
                })
                .onSuccess((outputValue) -> {
                    if (!this.complete())
                        next();
                });
    }

    void cancel() {
        if (this.scheduleFuture.isCompleted()) return;
        if (this.timerContext != null
                && !this.timerContext.isCancelled()
                && !this.timerContext.isExpired()) {
            this.timerContext.cancel();
        }

        this.scheduleFuture.complete(CANCEL_EXCEPTION.getMessage(),
                CANCEL_EXCEPTION);
    }
}
