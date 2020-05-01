package io.apef.core.utils.scheduler;

class ScheduleRetryImpl extends
        ScheduleImpl<Schedule.RetryContext>
        implements Schedule.RetryContext {
    ScheduleRetryImpl(Scheduler scheduler) {
        super(scheduler, false);
    }

    public boolean retry() {
        boolean next = next();
        if (!next) {
            super.end();
        }
        return next;
    }

    @Override
    protected void runTask(ScheduleTask<RetryContext> scheduleTask) {
        scheduleTask.run(this);
    }
}
