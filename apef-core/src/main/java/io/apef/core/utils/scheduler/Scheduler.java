package io.apef.core.utils.scheduler;

import io.apef.core.utils.ChannelTimer;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;

/**
 * UnThreadsafe Scheduler,
 */
public class Scheduler {
    private static class InstanceHolder {
        private final static Scheduler instance = new Scheduler();
        private final static HashedWheelTimer timer = ChannelTimer.timer();
    }

    private Scheduler() {
    }

    public static ScheduleBuilder<Schedule.ScheduleContext> schedule() {
        return new ScheduleGeneralImpl(InstanceHolder.instance);
    }

    public static ScheduleBuilder<Schedule.RetryContext> retry() {
        return new ScheduleRetryImpl(InstanceHolder.instance);
    }

    protected Timeout doSchedule(Schedule schedule, long delay) {

        return InstanceHolder.timer.newTimeout(timeout -> {
            schedule.run();
        }, delay, schedule.timeUnit());
    }

    protected Timeout doSchedule(Schedule schedule) {
        return InstanceHolder.timer.newTimeout(timeout -> {
            schedule.run();
        }, schedule.interval(), schedule.timeUnit());
    }
}
