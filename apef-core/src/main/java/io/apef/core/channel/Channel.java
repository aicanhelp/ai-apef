package io.apef.core.channel;

import io.apef.core.channel.executor.batch.BatchSchedule;
import io.apef.core.channel.executor.schedule.ChannelSchedule;
import io.apef.core.channel.executor.sequential.SequentialSchedule;
import io.apef.core.channel.executor.streaming.ChannelStream;
import io.apef.core.channel.future.ChannelFuture;
import io.apef.core.channel.message.ChannelMessage;

import java.util.concurrent.TimeUnit;

public interface Channel<C extends Channel<C>> {

    <C extends ChannelConfig> C channelConfig();

    /**
     * Write message
     *
     * @param message
     */
    void write(ChannelMessage message);

    /**
     * Specify messageType to write the message
     * Maybe, the specified messageType is not same with the actual message type
     *
     * @param messageType
     * @param message
     */
    void write(MessageType messageType, ChannelMessage message);

    <T, R, M extends ChannelMessage<T, R>> C handler(MessageType messageType, ChannelHandler<T, R, M> channelHandler);

    <T, R> C handler(MessageType messageType, ChannelHandler2<T, R> channelHandler);

    void execute(Runnable runnable);

    void schedule(Runnable runnable, long delay, TimeUnit timeUnit);

    <T> ChannelSchedule<T> schedule(T inputContext);

    <T> BatchSchedule<T> batch(T inputContext);

    <T>SequentialSchedule<T> sequential(T inputContext);

    <T> ChannelStream<T> stream(ChannelFuture<T> future);

    boolean started();

    C start();

    void close();
}
