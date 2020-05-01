package io.apef.core.channel;

import io.apef.core.channel.box.MessageBox;

import io.apef.core.channel.executor.ChannelExecutor;
import io.apef.core.channel.executor.batch.BatchSchedule;
import io.apef.core.channel.executor.schedule.ChannelSchedule;
import io.apef.core.channel.executor.sequential.SequentialSchedule;
import io.apef.core.channel.executor.streaming.ChannelStream;
import io.apef.core.channel.future.ChannelFuture;
import io.apef.core.channel.message.ChannelInternalRequestMessage;
import io.apef.core.channel.message.ChannelMessage;
import io.apef.core.channel.request.DefaultMessageType;
import io.apef.core.utils.ChannelTimer;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class AbstractChannel<C extends AbstractChannel<C>> implements
        Channel<C> {
    @Getter
    @Accessors(fluent = true)
    private ChannelConfig channelConfig;
    private MessageBox messageBox;
    private volatile boolean started;
    private ChannelExecutor channelExecutor;

    protected AbstractChannel(ChannelConfig channelConfig, MessageBox messageBox) {
        Preconditions.checkState(StringUtils.isNotBlank(channelConfig.getName()));
        this.channelConfig = channelConfig;
        this.messageBox = messageBox;
        this.channelExecutor = ChannelExecutor.newExecutor(this);
    }

    @Override
    public void write(ChannelMessage message) {
        this.messageBox.put(message.messageType(), message);
    }

    @Override
    public <T, R, M extends ChannelMessage<T, R>> C handler(MessageType messageType,
                                                            ChannelHandler<T, R, M> channelHandler) {
        if (started) {
            log.warn("Set handler must be before started");
        }
        this.messageBox.<M>handler(messageType,
                (messageType1, m) -> {
                    try {
                        if (m instanceof ChannelInternalRequestMessage) {
                            ChannelInternalRequestMessage<T, R> message = (ChannelInternalRequestMessage<T, R>) m;
                            message.channelPipe()
                                    .acceptor().accept(message,
                                    requestMessage -> channelHandler.handle(m));
                        } else {
                            channelHandler.handle(m);
                        }
                    } catch (Exception ex) {
                        m.messageContext().fail("Failed on handle message", ex);
                    }
                }
        );
        return (C) this;
    }

    @Override
    public <T, R> C handler(MessageType messageType, ChannelHandler2<T, R> channelHandler) {
        return this.<T, R, ChannelMessage<T, R>>handler(messageType, requestMessage -> {
            channelHandler.handle(requestMessage.messageContext(), requestMessage.requestContent());
        });
    }

    @Override
    public void write(MessageType messageType, ChannelMessage message) {
        this.messageBox.put(messageType, message);
    }

    @Override
    public boolean started() {
        return this.started;
    }

    public C start() {
        if (this.started) return (C) this;
        this.started = true;
        this.messageBox.<ChannelInternalRequestMessage>handler(DefaultMessageType.Response,
                (messageType1, message) -> {
                    message.channelPipe().sender().handleResponse(message);
                })
                .<ChannelInternalRequestMessage>handler(DefaultMessageType.Timeout, (messageType, message) -> {
                    message.finishByTimeout();
                })
                .<Runnable>handler(DefaultMessageType.Schedule, (messageType, message) -> {
                    message.run();
                });

        this.messageBox.start();
        return (C) this;
    }

    public void execute(Runnable runnable) {
        this.messageBox.put(DefaultMessageType.Schedule, runnable);
    }

    public void schedule(Runnable runnable, long delay, TimeUnit timeUnit) {
        if (delay < 0) {
            this.execute(runnable);
            return;
        }
        ChannelTimer.timer()
                .newTimeout(timeout -> {
                    this.messageBox.put(DefaultMessageType.Schedule, runnable);
                }, delay, timeUnit);
    }

    @Override
    public <T> ChannelSchedule<T> schedule(T inputContext) {
        return this.channelExecutor.schedule(inputContext);
    }

    @Override
    public <T> BatchSchedule<T> batch(T inputContext) {
        return this.channelExecutor.batch(inputContext);
    }

    @Override
    public <T> SequentialSchedule<T> sequential(T inputContext) {
        return this.channelExecutor.sequential(inputContext);
    }

    @Override
    public <T> ChannelStream<T> stream(ChannelFuture<T> future) {
        return this.channelExecutor.stream(future);
    }

    public void close() {
        if (!this.started) return;
        this.started = false;
        this.messageBox.close();
    }
}
