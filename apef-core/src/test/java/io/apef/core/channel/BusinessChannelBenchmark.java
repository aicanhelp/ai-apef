package io.apef.core.channel;

import io.apef.core.APEF;
import io.apef.core.channel.message.ChannelMessageContext;
import io.apef.core.channel.pipe.B2BPipe;
import io.apef.testing.benchmark.Benchmark;
import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusinessChannelBenchmark extends BaseUnitSpec {
    //Benchmark: ~500M/s
    @Test
    public void benchmarkSmallObject() {
        doBenchmark(() -> 1);
    }

    //Benchmark: ~500M/s
    @Test
    public void benchmarkBigObject() {
        BigObject bigObject = new BigObject();
        doBenchmark(() -> bigObject);
    }

    //~2000M
    @Test
    public void benchmarkExecute() {
        BusinessChannel businessChannel1 = APEF.createBusinessChannel(new ChannelConfig()
                .setName("business1").setQueueSize(8096));
        businessChannel1.start();
        Benchmark.benchmark()
                .threads(1)
                .rounds(10)
                .concurrency(1000)
                .iterations(10000)
                .benchmarkTask((index, runnerContext) -> {
                    businessChannel1.execute(() -> {
                        runnerContext.done(index);
                    });
                }).start();

        businessChannel1.close();
    }


    public void doBenchmark(ObjectCreator objectCreator) {
        BusinessChannel businessChannel1 = APEF.createBusinessChannel(new ChannelConfig()
                .setName("business1").setQueueSize(8096));
        BusinessChannel businessChannel2 = APEF.createBusinessChannel(new ChannelConfig()
                .setName("business2").setQueueSize(8096));
        businessChannel1.start();
        MessageType messageType = MessageType.newType((byte) 0);
        businessChannel2
                .handler(messageType, ChannelMessageContext::succeed)
                .start();

        B2BPipe<?, ?> pipe = businessChannel2.B2BPipe(businessChannel1);
        Benchmark.benchmark()
                .threads(1)
                .rounds(10)
                .concurrency(1000)
                .iterations(10000)
                .reportInterval(3)
                .benchmarkTask((index, runnerContext) -> {
                    pipe.request()
                            .messageType(messageType)
                            .requestContent(objectCreator.create())
                            .onSuccess((responseContent) -> {
                            })
                            .onFailure((errMsg, cause) -> {
                            })
                            .future()
                            .onFailure((errMsg, cause) -> {
                            })
                            .onSuccess((outputValue) ->
                                    runnerContext.done(index)
                            );
                }).start();

        businessChannel1.close();
        businessChannel2.close();
    }

    interface ObjectCreator {
        Object create();
    }

    static class BigObject {
        int a;
        boolean b;
        long c;
        String d;
        Map<String, String> data1 = new HashMap<>();
        List<String> data2 = new ArrayList<>();

        public BigObject() {
            for (int i = 0; i < 100; i++) {
                data1.put("Key_" + i, "Value_" + i);
                data2.add("Data2_" + i);
            }
        }
    }
}
