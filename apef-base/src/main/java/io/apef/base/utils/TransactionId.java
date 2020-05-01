package io.apef.base.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class TransactionId {
    private final static AtomicInteger value = new AtomicInteger(0);

    public static int next() {
        return value.incrementAndGet();
    }
}
