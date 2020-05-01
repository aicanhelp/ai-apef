package io.apef.core.channel.executor.schedule;

import io.apef.core.channel.future.ChannelFuture;
import io.apef.core.channel.future.ChannelFutureImpl;

public interface ChannelScheduleFuture<T> extends ChannelFuture<T> {
    void cancel();
}

class ChannelScheduleFutureImpl<T> extends ChannelFutureImpl<T>
        implements ChannelScheduleFuture<T> {
    private ChannelScheduleImpl<T> channelSchedule;

    public ChannelScheduleFutureImpl(ChannelScheduleImpl<T> channelSchedule) {
        this.channelSchedule = channelSchedule;
    }

    @Override
    public void cancel() {
        this.channelSchedule.cancel();
    }
}
