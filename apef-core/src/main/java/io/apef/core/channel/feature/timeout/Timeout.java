package io.apef.core.channel.feature.timeout;

import io.apef.core.channel.feature.ChannelMessageFeature;
import io.apef.core.channel.message.ChannelInternalRequestMessage;
import io.apef.core.utils.scheduler.Schedule;
import io.apef.core.utils.scheduler.ScheduleBuilder;
import io.apef.core.utils.scheduler.Scheduler;

public class Timeout extends ChannelMessageFeature {
    private long timeoutMs;
    private ScheduleBuilder timeoutScheduleBuilder;
    private Schedule timeoutSchedule;

    public Timeout(long timeoutMs, ChannelInternalRequestMessage channelMessage) {
        super(channelMessage);
        this.timeoutMs = timeoutMs;
        this.timeoutScheduleBuilder = Scheduler.schedule()
                .times(1)
                .delay(timeoutMs)
                .scheduleTask(scheduleContext -> channelMessage.timeout());
    }

    public long timeoutMs() {
        return this.timeoutMs;
    }

    public void start() {
        if (this.timeoutSchedule != null) return;
        this.timeoutSchedule = this.timeoutScheduleBuilder.start();
    }

    public void cancel() {
        if(this.timeoutSchedule==null) return;
        this.timeoutSchedule.cancel();
    }

    public boolean isDone() {
        return this.timeoutSchedule != null
                && this.timeoutSchedule.isDone();
    }
}
