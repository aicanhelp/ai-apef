package io.apef.core.channel.executor;

import io.apef.core.channel.Channel;
import io.apef.core.channel.executor.batch.BatchSchedule;
import io.apef.core.channel.executor.batch.BatchScheduleImpl;
import io.apef.core.channel.executor.schedule.ChannelSchedule;
import io.apef.core.channel.executor.schedule.ChannelScheduleImpl;
import io.apef.core.channel.executor.sequential.SequentialSchedule;
import io.apef.core.channel.executor.sequential.SequentialScheduleImpl;
import io.apef.core.channel.executor.streaming.ChannelStream;
import io.apef.core.channel.executor.streaming.ChannelStreamImpl;
import io.apef.core.channel.future.ChannelFuture;

public interface ChannelExecutor {

    static ChannelExecutor newExecutor(Channel<?> channel) {
        return new ChannelExecutor() {
            @Override
            public <T> BatchSchedule<T> batch(T inputContext) {
                return new BatchScheduleImpl<T>(channel)
                        .withOutputContext(inputContext);
            }

            @Override
            public <T> ChannelSchedule<T> schedule(T inputContext) {
                return new ChannelScheduleImpl<T>(channel)
                        .withOutputContext(inputContext);
            }

            @Override
            public <T> SequentialSchedule<T> sequential(T inputContext) {
                return new SequentialScheduleImpl<T>(channel)
                        .withOutputContext(inputContext);
            }

            @Override
            public <T> ChannelStream<T> stream(ChannelFuture<T> future) {
                return new ChannelStreamImpl<T>(future);
            }
        };
    }

    <T> BatchSchedule<T> batch(T outputContext);

    <T> ChannelSchedule<T> schedule(T outputContext);

    <T> SequentialSchedule<T> sequential(T outputContext);

    <T> ChannelStream<T> stream(ChannelFuture<T> future);

    interface FutureTask<T> {
        ChannelFuture run(T outputContext);
    }

    interface CompleteSupplier<T> {
        boolean isCompleted(T outputContext);
    }

    interface OnOverTimes<T> {
        void onOverTimes(T outputContext);
    }

}
