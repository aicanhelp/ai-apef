package io.apef.core.event;

public interface EventHandler<E> {
    void handle(E eventBody);
}
