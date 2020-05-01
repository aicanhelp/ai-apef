package io.apef.core.channel.box;

import io.apef.core.channel.TestMessageType;
import io.apef.testing.unit.BaseUnitSpec;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class MessageBoxBenchmark extends BaseUnitSpec {

    protected void doBenchmark(MessageBoxCreator messageBoxCreator, int typeCount) {
        for (int k = 0; k < 5; k++) {
            MessageBox messageBox = messageBoxCreator.create();
            final int total = 10000000;
            Blocker blocker = new Blocker();
            final AtomicInteger counter = new AtomicInteger();
            for (int i = 0; i < typeCount; i++) {
                messageBox.handler(TestMessageType.values()[i], (messageType, message) -> {
                    blocker.endIf(counter.incrementAndGet() == total);
                });
            }
            messageBox.start();

            Random random = new Random();
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < total; i++) {
                messageBox.put(TestMessageType.values()[random.nextInt(typeCount)], 1);
            }
            blocker.awaitEnd();
            log.info("Spent time: " + (System.currentTimeMillis() - startTime));
        }
    }

    protected void doBenchmark2(MessageBoxCreator messageBoxCreator, int actors) {
        for (int k = 0; k < 5; k++) {
            MessageBox messageBox1 = messageBoxCreator.create();
            MessageBox messageBox2 = messageBoxCreator.create();
            final int total = actors * 1000;
            Blocker blocker = new Blocker();

            AtomicInteger finished = new AtomicInteger();
            messageBox1.handler(TestMessageType.values()[0], (messageType, message) -> {
                if (((MessageObject) message).value < actors) {
                    messageBox2.put(messageType, message);
                } else {
                    finished.addAndGet(actors);
                }
                if (finished.get() == total)
                    blocker.end();
            });
            messageBox2.handler(TestMessageType.values()[0], (messageType, message) -> {
                ((MessageObject) message).value = ((MessageObject) message).value + 1;
                messageBox1.put(messageType, message);
            });
            messageBox1.start();
            messageBox2.start();

            long startTime = System.currentTimeMillis();

            for (int i = 0; i < actors; i++)
                messageBox1.put(TestMessageType.values()[0], new MessageObject());
            blocker.awaitEnd();
            log.info("TPS: " + total / (System.currentTimeMillis() - startTime));
        }
    }

    class MessageObject {
        private boolean isReturn;
        private int value = 0;
    }

    interface MessageBoxCreator {
        MessageBox create();
    }
}
