package io.apef.core.event;

public class EventService {
    private IEventClass eventClass;
    private IEventBus eventBus;

    public EventService(IEventClass eventClass, IEventBus eventBus) {
        this.eventClass = eventClass;
        this.eventBus = eventBus;
    }

    public <T> EventConsumer<T> consume(IEventType eventType, int workers, EventHandler<T> handler) {
        return this.eventBus.consume(this.eventClass, eventType, workers, handler);
    }

    public <T> EventListener<T> listen(IEventType eventType, EventHandler<T> handler) {
        return this.eventBus.listen(this.eventClass, eventType, handler);
    }

    public <T> EventPublisher<T> publisher(IEventType eventType) {
        return this.eventBus.publisher(this.eventClass, eventType);
    }
}
