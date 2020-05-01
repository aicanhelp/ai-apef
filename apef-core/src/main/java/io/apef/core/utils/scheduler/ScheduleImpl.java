package io.apef.core.utils.scheduler;

import io.netty.util.Timeout;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.concurrent.TimeUnit;

@Setter
@Accessors(fluent = true)
@Getter(AccessLevel.PUBLIC)
abstract class ScheduleImpl<T extends Schedule.ScheduleContext>
        implements Schedule,
        ScheduleBuilder<T> {

    private long delay = 0;
    private int times = -1;
    private int currentTimes = 0;
    private long interval = -1;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private ScheduleTask<T> scheduleTask;
    private boolean isCanceled;

    private Timeout timeout;
    private boolean started = false;

    private volatile boolean isDone;

    private Scheduler scheduler;

    private boolean autoNext;

    ScheduleImpl(Scheduler scheduler, boolean autoNext) {
        this.scheduler = scheduler;
        this.autoNext = autoNext;
    }

    public void run() {
        if (this.isDone || this.isCanceled) return;

        if (this.times > 0 && this.currentTimes++ >= this.times) {
            this.isDone = true;
            return;
        }

        this.runTask(this.scheduleTask);
        if (this.autoNext) {
            next();
        }
    }

    protected abstract void runTask(ScheduleTask<T> scheduleTask);

    public Schedule start() {
        if (this.interval < 0 && times < 1) times = 1;
        if (this.scheduleTask == null) {
            throw new IllegalArgumentException("ScheduleTask is required");
        }
        if (started) return this;
        started = true;
        if (delay > 0)
            this.timeout = this.scheduler.doSchedule(this, this.delay);
        else
            this.run();
        return this;
    }

    protected boolean next() {
        if (this.isDone) return false;
        this.timeout = this.scheduler.doSchedule(this);
        return true;
    }

    public void cancel() {
        if (this.isDone || this.isCanceled) return;
        if (this.timeout != null
                && !this.timeout.isCancelled()
                && !this.timeout.isExpired()) {
            this.timeout.cancel();
        }
        this.isCanceled = true;
    }

    public void end() {
        this.isDone = true;
    }

    public int timeIndex() {
        return this.currentTimes;
    }
}
