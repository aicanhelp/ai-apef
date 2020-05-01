package io.apef.core.event;

import io.apef.core.event.disruptor.DisruptorEventBus;
import io.apef.core.event.disruptor.DisruptorEventBusConfig;
import io.apef.testing.unit.BaseUnitSpec;
import io.apef.testing.unit.UnitSpec;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public abstract class EventBusTest extends BaseUnitSpec {

    protected void testBus(DisruptorEventBusConfig config,
                           int eventTypeCount,
                           int messageClassCount,
                           int handlerCount,
                           int publisherCount,
                           int messageCount) {
        DisruptorEventBus eventBus =
                new DisruptorEventBus(config);

        Vertx vertx = Vertx.vertx(new VertxOptions()
                .setEventLoopPoolSize(4).setWorkerPoolSize(16));
        Tester[] testers = createTesters(vertx, eventBus, eventTypeCount, messageClassCount, handlerCount,
                publisherCount, messageCount);
        eventBus.start();
        long startTime = System.currentTimeMillis();
        Stream.of(testers).forEach(Tester::run);

        blockingMsUntil(10000,() -> {
            for (Tester tester : testers) {
                if (!tester.isFinished()) return false;
            }
            return true;
        });
        log.info("Spent Time: " + (System.currentTimeMillis() - startTime));
        for (Tester tester : testers) {
            log.info(tester.toString());
        }
    }

    private static class Message {
        private int count;

    }

    private static Tester[] createTesters(Vertx vertx,
                                          IEventBus eventBus,
                                          int eventTypeCount,
                                          int messageClassCount,
                                          int handlerCount,
                                          int publisherCount,
                                          int count) {
        TesterBuilder testerBuilder = new TesterBuilder(vertx, eventBus, handlerCount, publisherCount, count);
        Tester[] testers = new Tester[eventTypeCount * messageClassCount];
        int index = 0;
        for (int i = 0; i < messageClassCount; i++) {
            if (eventTypeCount > 0) {
                testers[index++] = testerBuilder.build(EventClass.values()[i], EventType.EventTypeA,
                        (eventType, value) -> new MessageA());
            }
            if (eventTypeCount > 1) {
                testers[index++] = testerBuilder.build(EventClass.values()[i], EventType.EventTypeB,
                        (eventType, value) -> new MessageB());
            }
            if (eventTypeCount > 2) {
                testers[index++] = testerBuilder.build(EventClass.values()[i], EventType.EventTypeC,
                        (eventType, value) -> new MessageC());
            }
            if (eventTypeCount > 3) {
                testers[index++] = testerBuilder.build(EventClass.values()[i], EventType.EventTypeD,
                        (eventType, value) -> new MessageD());
            }
            if (eventTypeCount > 4) {
                testers[index++] = testerBuilder.build(EventClass.values()[i], EventType.EventTypeE,
                        (eventType, value) -> new MessageE());
            }
        }
        return testers;
    }

    private static class TesterBuilder {
        Vertx vertx;
        IEventBus eventBus;
        int handlerCount;
        int publisherCount;
        int messageCount;

        public TesterBuilder(Vertx vertx,
                             IEventBus eventBus, int handlerCount,
                             int publisherCount,
                             int count) {
            this.vertx = vertx;
            this.eventBus = eventBus;
            this.handlerCount = handlerCount;
            this.publisherCount = publisherCount;
            this.messageCount = count;
        }

        public Tester build(IEventClass eventClass, IEventType eventType, MessageCreator messageCreator) {
            return new Tester(vertx, eventBus, eventType, eventClass, handlerCount, publisherCount, messageCount) {
                @Override
                protected Message createMessage(IEventType eventType, int value) {
                    return messageCreator.createMessage(eventType, value);
                }
            };
        }
    }

    private interface MessageCreator {
        Message createMessage(IEventType eventType, int value);
    }

    private static abstract class Tester {
        Vertx vertx;
        IEventBus eventBus;
        IEventType eventType;
        IEventClass eventClass;
        int handlerCount;
        int publisherCount;
        int messageCount;
        EventPublisher<Message>[] publishers;
        EventListener[] consumers;
        volatile AtomicInteger[] receivers;

        protected Tester(Vertx vertx, IEventBus eventBus,
                         IEventType eventType, IEventClass eventClass,
                         int handlerCount,
                         int publisherCount, int messageCount) {
            this.vertx = vertx;
            this.eventBus = eventBus;
            this.eventType = eventType;
            this.eventClass = eventClass;
            this.handlerCount = handlerCount;
            this.publisherCount = publisherCount;
            this.messageCount = messageCount;
            this.publishers = new EventPublisher[publisherCount];
            this.consumers = new EventListener[handlerCount];
            this.receivers = new AtomicInteger[handlerCount];
            for (int i = 0; i < publisherCount; i++) {
                this.publishers[i] = eventBus.publisher(this.eventClass, this.eventType);
            }
            for (int i = 0; i < handlerCount; i++) {
                final int k = i;
                receivers[i] = new AtomicInteger();
                this.consumers[i] = eventBus.listen(this.eventClass,
                        this.eventType,
                        (EventHandler<Message>) aMessage -> {
                            receivers[k].incrementAndGet();
                        }
                );
            }
        }

        public void run() {
            for (int i = 0; i < publisherCount; i++) {
                final int k = i;
                vertx.runOnContext(aVoid -> {
                    for (int j = 0; j < messageCount; j++) {
                        publishers[k].publish(createMessage(eventType, (k + 1) * j));
                    }
                });
            }
        }

        public boolean isFinished() {
            for (AtomicInteger receiver : receivers) {
                if (receiver.get() != publisherCount * messageCount) return false;
            }
            return true;
        }

        protected abstract Message createMessage(IEventType eventType, int value);

        @Override
        public String toString() {
            return this.eventType + "," + this.eventClass + ", Received:" + UnitSpec.toSString(this.receivers);
        }
    }


    private static class MessageA extends Message {

    }

    private static class MessageB extends Message {
    }

    private static class MessageC extends Message {
    }

    private static class MessageD extends Message {
    }

    private static class MessageE extends Message {

    }

    protected enum EventType implements IEventType {
        EventTypeA, EventTypeB, EventTypeC, EventTypeD, EventTypeE;
    }

    protected enum EventClass implements IEventClass {
        Event1, Event2, Event3;

        @Override
        public String value() {
            return this.name();
        }
    }
}
