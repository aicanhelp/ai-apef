package io.apef.connector.base;


import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Accessors(fluent = true)
public class MaxAccepted {
    private int maxAccepted;

    private AtomicInteger accepted = new AtomicInteger();

    public MaxAccepted(int maxAccepted) {
        this.maxAccepted = maxAccepted;
    }

    public boolean acceptNext() {
        if (this.accepted.get() > this.maxAccepted) return false;
        this.accepted.incrementAndGet();
        return true;
    }

    public void down() {
        this.accepted.decrementAndGet();
    }
}
