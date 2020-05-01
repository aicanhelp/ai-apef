package io.apef.core.event.disruptor;

import io.apef.core.event.*;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;
import com.lmax.disruptor.util.DaemonThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Performance: >20m/s
 */
public class DisruptorEventBus extends AEventBus {
    private static Logger log = LoggerFactory.getLogger(DisruptorEventBus.class);
    private DisruptorEventBusConfig config;
    private Map<IEventClass, DisruptorBus> buses = new ConcurrentHashMap<>();

    public DisruptorEventBus(DisruptorEventBusConfig config) {
        this.config = config;
    }

    @Override
    protected ASubEventBus newSubBus(IEventClass eventClass) {
        return new DisruptorBus(eventClass, this.config.config(eventClass));
    }

    @Override
    public void beforeClose() {
        this.buses.forEach((eventType, disruptorBus) -> disruptorBus.close());
    }

    private static class DisruptorPublisher<T> extends EventPublisher<T> {
        DisruptorBus disruptorBus;

        DisruptorPublisher(DisruptorBus disruptorBus, IEventType eventType, IEventClass eventClass) {
            super(eventClass, eventType);
            this.disruptorBus = disruptorBus;
        }

        @Override
        public void publish(T eventBody) {
            RingBuffer ringBuffer = disruptorBus.disruptor.getRingBuffer();
            long sequence = ringBuffer.next();

            DisruptorEvent event = (DisruptorEvent) ringBuffer.get(sequence);
            event.eventType(eventType()).eventClass(eventClass())
                    .eventBody(eventBody);

            ringBuffer.publish(sequence);
        }
    }

    private static class DisruptorConsumer extends EventConsumer {
        protected DisruptorConsumer(IEventClass eventClass, IEventType eventType,
                                    int workers,
                                    io.apef.core.event.EventHandler eventHandler) {
            super(eventClass, eventType, workers, eventHandler);
        }
    }

    private static class DisruptorListener extends EventListener {
        protected DisruptorListener(IEventClass eventClass, IEventType eventType,
                                    io.apef.core.event.EventHandler eventHandler) {
            super(eventClass, eventType, eventHandler);
        }
    }

    private static class DisruptorBus extends ASubEventBus {
        DisruptorEventBusConfig.DisruptorConfig config;
        Disruptor<DisruptorEvent> disruptor;
        EventHandlerGroup<DisruptorEvent> eg;

        DisruptorBus(IEventClass eventClass, DisruptorEventBusConfig.DisruptorConfig disruptorConfig) {
            super(eventClass);
            this.config = disruptorConfig;
        }

        void init() {
            int bufferSize = this.config.getBufferSize();
            this.disruptor = new Disruptor<>(new BusEventFactory(),
                    bufferSize,
                    DaemonThreadFactory.INSTANCE);

            this.consumers.values().forEach(eventConsumer -> {
                WorkHandler<DisruptorEvent>[] workHandlers = new WorkHandler[eventConsumer.workers()];
                for (int i = 0; i < eventConsumer.workers(); i++) {
                    workHandlers[i] = disruptorEvent -> {
                        if (disruptorEvent.eventBody() != null &&
                                disruptorEvent.eventType() == eventConsumer.eventType()
                                && eventConsumer.active()) {
                            eventConsumer.eventHandler().handle(disruptorEvent.eventBody());
                        }
                    };
                }
                if (eg == null) {
                    eg = disruptor.handleEventsWithWorkerPool(workHandlers);
                } else {
                    eg = eg.handleEventsWithWorkerPool(workHandlers);
                }
            });

            this.listeners.values().forEach(listeners -> listeners.forEach(disruptorListener -> {
                EventHandler<DisruptorEvent> eventHandler = (disruptorEvent, l, b) -> {
                    if (disruptorEvent.eventBody() != null &&
                            disruptorEvent.eventType() == disruptorListener.eventType()
                            && disruptorListener.active()) {
                        disruptorListener.handle(disruptorEvent.eventBody());
                    }
                };
                if (eg == null) {
                    eg = disruptor.handleEventsWith(eventHandler);
                } else {
                    eg = eg.handleEventsWith(eventHandler);
                }
            }));

            if (eg != null) {
                eg.then((event, sequence, endOfBatch) -> event.eventBody(null));
            } else {
                disruptor.handleEventsWith((event, sequence, endOfBatch) -> event.eventBody(null));
            }

        }

        @Override
        public synchronized void start() {
            this.init();
            this.disruptor.start();
        }

        @Override
        protected EventConsumer newConsumer(IEventType eventType, int workers,
                                            io.apef.core.event.EventHandler handler) {
            return new DisruptorConsumer(this.eventClass, eventType, workers, handler);
        }

        @Override
        protected EventListener newListener(IEventType eventType, io.apef.core.event.EventHandler handler) {
            return new DisruptorListener(this.eventClass, eventType, handler);
        }

        @Override
        protected EventPublisher newPublisher(IEventType eventType) {
            return new DisruptorPublisher(this, eventType, eventClass);
        }

        void close() {
            if (this.disruptor != null) {
                this.disruptor.shutdown();
            }
        }
    }

    private static class DisruptorEvent extends EventMessage {
    }

    private static class BusEventFactory implements EventFactory<DisruptorEvent> {

        @Override
        public DisruptorEvent newInstance() {
            return new DisruptorEvent();
        }
    }
}
