package io.apef.core.channel.box;

import io.apef.core.channel.MessageType;
import com.google.common.base.Preconditions;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ThreadFactory;

@Slf4j
public class DisruptorMessageBox implements MessageBox {
    @Getter
    @Accessors(fluent = true)
    private MessageBoxId messageBoxId;
    private Disruptor<MessageEvent> disruptor;
    private int workers = 1;
    private volatile boolean started;
    private HandlerChain handlerChain;

    public DisruptorMessageBox(String name, int size, int workers, ThreadFactory threadFactory) {
        Preconditions.checkState(StringUtils.isNotBlank(name));
        this.messageBoxId = new MessageBoxId(name);
        this.disruptor = new Disruptor<>(MessageEvent::new,
                calculateBufferSize(size),
                threadFactory);

        if (workers > 0) this.workers = workers;
    }

    private static int calculateBufferSize(int bufferSize) {
        if (bufferSize <= 0) return 128;
        return (int) Math.round(Math.pow(2, Math.ceil(Math.log(bufferSize) / Math.log(2))));
    }

    public DisruptorMessageBox put(MessageType messageType, Object message) {
        RingBuffer ringBuffer = disruptor.getRingBuffer();
        long sequence = ringBuffer.next();

        MessageEvent event = (MessageEvent) ringBuffer.get(sequence);
        event.message = message;
        event.messageType = messageType;

        ringBuffer.publish(sequence);
        return this;
    }

    public <T> DisruptorMessageBox handler(MessageType messageType, MessageBoxHandler<T> handler) {
        if (handlerChain != null) {
            handlerChain.addHandler(messageType, handler);
            return this;
        }
        handlerChain = new HandlerChain(messageType, handler);
        if (this.workers == 1) {
            EventHandler<MessageEvent> eventHandler = (event, l, b) -> {
                try {
                    if (!handlerChain.handleEvent(event)) {
                        log.warn("No Handler for message Type: " + event.messageType);
                    }
                } catch (Exception ex) {
                    log.error("Exception thrown in handling event: " + event.message, ex);
                }
                event.reset();
            };

            disruptor.handleEventsWith(eventHandler);
        } else {
            WorkHandler<MessageEvent>[] workHandlers = new WorkHandler[this.workers];
            for (int i = 0; i < this.workers; i++) {
                workHandlers[i] = event -> {
                    try {
                        if (!handlerChain.handleEvent(event)) {
                            log.warn("No Handler for message Type: " + event.messageType);
                        }
                    } catch (Exception ex) {
                        log.error("Exception thrown in handling event: " + event.message, ex);
                    }
                    event.reset();
                };
            }

            disruptor.handleEventsWithWorkerPool(workHandlers);
        }

        return this;
    }

    public DisruptorMessageBox start() {
        if (this.started) return this;
        this.started = true;
        this.disruptor.start();
        return this;
    }

    public void close() {
        if (!this.started) return;
        this.started = false;
        this.disruptor.shutdown();
    }
}
