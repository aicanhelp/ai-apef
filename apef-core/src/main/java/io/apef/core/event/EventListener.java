package io.apef.core.event;

import java.util.ArrayList;
import java.util.List;

public class EventListener<E> extends EventProcessor<E> {
    private List<EventListener<E>> subListeners = new ArrayList<>();

    protected EventListener(IEventClass eventClass,
                            IEventType eventType,
                            EventHandler<E> eventHandler) {
        super(eventClass, eventType, eventHandler);
    }

    public EventListener subListen(EventHandler<E> eEventHandler) {
        EventListener<E> subListener = new EventListener<>(this.eventClass(),
                this.eventType(),
                eEventHandler);

        this.subListeners.add(subListener);
        return subListener;
    }

    public void handle(E body) {
        this.eventHandler().handle(body);
        this.subListeners.forEach(eEventListener -> {
            eEventListener.eventHandler().handle(body);
        });
    }
}
