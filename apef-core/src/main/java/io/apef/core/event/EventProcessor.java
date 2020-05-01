package io.apef.core.event;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public abstract class EventProcessor<E> extends EventActor {
    EventHandler<E> eventHandler;
    boolean active = true;

    protected EventProcessor(IEventClass eventClass,
                             IEventType eventType,
                             EventHandler<E> eventHandler) {
        super(eventClass, eventType);
        this.eventHandler = eventHandler;
    }

    public synchronized void activate() {
        this.active = true;
    }

    public synchronized void deactivate() {
        this.active = false;
    }
}
