package io.apef.core.utils.scheduler;

class ScheduleGeneralImpl
        extends ScheduleImpl<Schedule.ScheduleContext>
        implements Schedule.ScheduleContext {
    ScheduleGeneralImpl(Scheduler scheduler) {
        super(scheduler, true);
    }

    @Override
    protected void runTask(ScheduleTask<ScheduleContext> scheduleTask) {
        scheduleTask.run(this);
    }
}
