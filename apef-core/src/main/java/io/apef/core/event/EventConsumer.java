package io.apef.core.event;

import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
public class EventConsumer<E> extends EventProcessor<E> {
    private int workers;

    protected EventConsumer(IEventClass eventClass,
                            IEventType eventType,
                            int workers,
                            EventHandler<E> eventHandler) {
        super(eventClass, eventType, eventHandler);

        this.workers = workers < 1 ? 1 : workers;
    }
}
