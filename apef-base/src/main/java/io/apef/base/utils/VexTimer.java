package io.apef.base.utils;


import io.netty.util.HashedWheelTimer;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class VexTimer {
    private static class DefaultInstanceHolder {
        private final static HashedWheelTimer timer = timer();

        private static HashedWheelTimer timer() {
            HashedWheelTimer timer = new HashedWheelTimer(
                    new DefaultThreadFactory("Channel-Timer"),
                    10, TimeUnit.MILLISECONDS);
            timer.start();
            log.info("Default Channel-Timer started.");
            return timer;
        }
    }

    public static HashedWheelTimer timer() {
        return DefaultInstanceHolder.timer;
    }

    public static HashedWheelTimer newTimer(String name) {
        HashedWheelTimer timer = new HashedWheelTimer(
                new DefaultThreadFactory(name + "_ThreadFactory"),
                10, TimeUnit.MILLISECONDS);
        timer.start();
        return timer;
    }
}
