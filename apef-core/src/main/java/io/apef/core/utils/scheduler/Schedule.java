package io.apef.core.utils.scheduler;

import java.util.concurrent.TimeUnit;

public interface Schedule {

    long delay();

    int times();

    long interval();

    TimeUnit timeUnit();

    boolean isDone();

    void run();

    void cancel();

    interface ScheduleTask<T extends ScheduleContext> {
        void run(T scheduleContext);
    }

    interface ScheduleContext {
        int timeIndex();

        void end();
    }

    interface RetryContext extends ScheduleContext {
        boolean retry();
    }

}
