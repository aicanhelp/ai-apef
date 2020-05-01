package io.apef.core.utils.scheduler;

import java.util.concurrent.TimeUnit;

public interface ScheduleBuilder<T extends Schedule.ScheduleContext> {
    ScheduleBuilder<T> delay(long delay);

    ScheduleBuilder<T> times(int times);

    ScheduleBuilder<T> interval(long interval);

    ScheduleBuilder<T> timeUnit(TimeUnit timeUnit);

    ScheduleBuilder<T> scheduleTask(Schedule.ScheduleTask<T> scheduleTask);

    Schedule start();
}
