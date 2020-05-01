package io.apef.core.event;

import io.apef.core.event.disruptor.DisruptorEventBusConfig;
import org.testng.annotations.Test;

@Test(enabled = false)
public class DisruptorEventBusTest extends EventBusTest {

    public void testDisruptorBus1() {
        DisruptorEventBusConfig config = new DisruptorEventBusConfig();
        config.put(EventClass.Event1, new DisruptorEventBusConfig.DisruptorConfig());
        config.put(EventClass.Event2, new DisruptorEventBusConfig.DisruptorConfig());
        config.put(EventClass.Event3, new DisruptorEventBusConfig.DisruptorConfig());

        //base tests
        this.testBus(config, 1, 1, 1, 1, 10);
//        this.testBus(config, 3, 1, 1, 1, 10);
//        this.testBus(config, 1, 3, 1, 1, 10);
        this.testBus(config, 1, 1, 3, 1, 10);
//        this.testBus(config, 1, 1, 1, 3, 10);
//        this.testBus(config, 3, 3, 3, 3, 10);
//        //performance tests
        //this.testBus(config, 1, 1, 1, 1, 1000000);
//
//        this.testBus(config, 3, 1, 1, 1, 1000000);
//
//        this.testBus(config, 1, 3, 1, 1, 200000);
//
//        this.testBus(config, 3, 3, 3, 3, 10000);
    }

    public void testDisruptorBus2() {
        DisruptorEventBusConfig config = new DisruptorEventBusConfig();
        config.put(EventClass.Event1, new DisruptorEventBusConfig.DisruptorConfig());
        config.put(EventClass.Event2, new DisruptorEventBusConfig.DisruptorConfig());

//        //performance tests
        this.testBus(config, 1, 1, 1, 2, 10000000);
    }

    public void testDisruptorBus3() {
        DisruptorEventBusConfig config = new DisruptorEventBusConfig();
        config.put(EventClass.Event1, new DisruptorEventBusConfig.DisruptorConfig());
        //config.put(EventType.Event2, new DisruptorEventBusConfig.DisruptorConfig());

//        //performance tests
        this.testBus(config, 1, 1, 1, 1, 10000000);
    }
}