package io.apef.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AEventBus implements IEventBus {
    private static Logger log = LoggerFactory.getLogger(AEventBus.class);
    private Map<IEventClass, ASubEventBus> subBuses = new HashMap<>();
    protected volatile boolean started = false;

    protected static abstract class ASubEventBus {
        protected IEventClass eventClass;
        protected Map<IEventType, EventPublisher> publishers = new HashMap<>();
        protected Map<IEventType, EventConsumer> consumers = new HashMap<>();
        protected Map<IEventType, List<EventListener>> listeners = new HashMap<>();

        protected ASubEventBus(IEventClass eventClass) {
            this.eventClass = eventClass;
        }

        public <E> EventConsumer<E> consumer(IEventType eventType) {
            return this.consumers.get(eventType);
        }

        public <E> List<EventListener<E>> listeners(IEventType eventType) {
            return this.listeners.get(eventType).stream()
                    .map(eventListener -> (EventListener<E>) eventListener).collect(Collectors.toList());
        }

        public synchronized <E> EventConsumer<E> consumer(IEventType eventType, int workers, EventHandler handler) {
            EventConsumer consumer = this.consumers.get(eventType);
            if (consumer != null) {
                throw new IllegalArgumentException("Consumer has already set for eventClass: " + this.eventClass + ", eventType:" + eventType);
            }
            consumer = this.newConsumer(eventType, workers, handler);
            this.consumers.put(eventType, consumer);
            return consumer;
        }

        public synchronized <E> EventListener<E> listener(IEventType eventType, EventHandler handler) {
            List<EventListener> listenerList = this.listeners.get(eventType);
            if (listenerList == null) {
                listenerList = new ArrayList<>();
                this.listeners.put(eventType, listenerList);
            }
            EventListener listener = this.newListener(eventType, handler);
            listenerList.add(listener);
            return listener;
        }

        public synchronized <E> EventPublisher<E> publisher(IEventType eventType) {
            EventPublisher publisher = this.publishers.get(eventType);
            if (publisher == null) {
                publisher = this.newPublisher(eventType);
                this.publishers.put(eventType, publisher);
            }

            return publisher;
        }

        public abstract void start();

        protected abstract EventConsumer newConsumer(IEventType eventType, int workers, EventHandler handler);

        protected abstract EventListener newListener(IEventType eventType, EventHandler handler);

        protected abstract EventPublisher newPublisher(IEventType eventType);
    }

    @Override
    public synchronized <E> EventPublisher<E> publisher(IEventClass eventClass, IEventType eventType) {
        return this.getSubBus(eventClass).publisher(eventType);
    }

    @Override
    public <E> EventConsumer<E> consume(IEventClass eventClass, IEventType eventType,
                                        int workers, EventHandler<E> eventHandler) {
        this.checkNotStarted();
        return this.getSubBus(eventClass).consumer(eventType, workers, eventHandler);
    }

    @Override
    public <E> EventListener<E> listen(IEventClass eventClass, IEventType eventType,
                                       EventHandler<E> eventHandler) {
        this.checkNotStarted();
        return this.getSubBus(eventClass).listener(eventType, eventHandler);
    }

    @Override
    public <E> EventConsumer<E> consumer(IEventClass eventClass, IEventType eventType) {
        return this.getSubBus(eventClass).consumer(eventType);
    }

    @Override
    public <E> List<EventListener<E>> listeners(IEventClass eventClass, IEventType eventType) {
        return this.getSubBus(eventClass).listeners(eventType);
    }

    private ASubEventBus getSubBus(IEventClass eventClass) {
        ASubEventBus subBus = subBuses.get(eventClass);
        if (subBus == null) {
            subBus = this.newSubBus(eventClass);
            ASubEventBus old = subBuses.putIfAbsent(eventClass, subBus);
            if (old != null) {
                subBus = old;
            }
        }
        return subBus;
    }

    private void checkNotStarted() {
        if (this.started) {
            throw new IllegalArgumentException("Can not operated after Event is started. ");
        }
    }

    @Override
    public synchronized void start() {
        if (started) return;
        this.subBuses.forEach((iEventClass, aSubEventBus) -> {
            aSubEventBus.start();
        });
        started = true;
    }

    @Override
    public void close() {
        this.beforeClose();
        started = false;
    }

    protected abstract void beforeClose();

    protected abstract ASubEventBus newSubBus(IEventClass eventClass);
}
