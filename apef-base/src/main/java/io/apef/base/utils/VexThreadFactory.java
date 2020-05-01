package io.apef.base.utils;

import java.util.concurrent.ThreadFactory;

public class VexThreadFactory implements ThreadFactory {

    private int threadCounter = 0;
    private String namePrefix = null;

    public VexThreadFactory(String name) {
        this.namePrefix = "VEX-" + name + "-";
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName(namePrefix + (++threadCounter));
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }

}
