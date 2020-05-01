package io.apef.core.event;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public abstract class EventActor {
    private IEventClass eventClass;
    private IEventType eventType;

    protected EventActor(IEventClass eventClass, IEventType eventType) {
        this.eventClass = eventClass;
        this.eventType = eventType;
    }
}
