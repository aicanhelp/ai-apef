package io.apef.core.event;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public class EventMessage {
    protected IEventClass eventClass;
    protected IEventType eventType;

    protected Object eventBody;

    protected EventMessage(){}
}
