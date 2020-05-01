package io.apef.core.event;

public abstract class EventPublisher<T> extends EventActor {
    protected EventPublisher(IEventClass eventClass, IEventType eventType) {
        super(eventClass, eventType);
    }

    public abstract void publish(T eventBody);
}
