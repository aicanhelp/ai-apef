package io.apef.core.event;


import java.util.List;

public interface IEventBus {
    <E> EventPublisher<E> publisher(IEventClass eventClass, IEventType eventType);

    /**
     * Set Event Consumer
     * One event type only has one consumer,
     * One event is only consumed by one consumer worker.
     *
     * @param eventClass
     * @param eventType
     * @param eventHandler
     * @param <E>
     * @return
     */
    <E> EventConsumer<E> consume(IEventClass eventClass,
                                 IEventType eventType,
                                 int workers,
                                 EventHandler<E> eventHandler);

    /**
     * Get Event Consumer with eventClass and eventType
     *
     * @param eventClass
     * @param eventType
     * @param <E>
     * @return
     */
    <E> EventConsumer<E> consumer(IEventClass eventClass,
                                  IEventType eventType);

    /**
     * Add Event Listener,
     * all listeners can all events.
     *
     * @param eventClass
     * @param eventType
     * @param eventHandler
     * @param <E>
     * @return
     */
    <E> EventListener<E> listen(IEventClass eventClass, IEventType eventType, EventHandler<E> eventHandler);


    /**
     * Get Event Listener with eventClass and eventType
     *
     * @param eventClass
     * @param eventType
     * @param <E>
     * @return
     */
    <E> List<EventListener<E>> listeners(IEventClass eventClass, IEventType eventType);

    void start();

    void close();
}
